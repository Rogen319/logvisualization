package escore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.bean.TraceStatus;
import escore.config.MyConfig;
import escore.response.GetNodesListResponse;
import escore.response.GetPodsListResponse;
import escore.util.Const;
import escore.util.ESUtil;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ESCoreScheduledService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

//    private MyConfig myConfig = new MyConfig();//This way will say: nullpointer exception

    @Autowired
    private MyConfig myConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ESUtil esUtil;

    private String prevTimestamp = "1970-01-01T00:00:00.000Z";

    //Update pod info
    public void updatePodInfo(){
        TransportClient client = myConfig.getESClient();
        log.info("===Update the pod info===");
        List<PodInfo> originPods = esUtil.getStoredPods();
        //Get current pods information
        GetPodsListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getPodsList",
                GetPodsListResponse.class);
        if(result.isStatus()){
            ObjectMapper mapper = new ObjectMapper();
            List<PodInfo> currentPods = result.getPods();
            try{
                for(PodInfo pod : currentPods){
                    if(!existInOrginPods(pod, originPods)){
//                        log.info(String.format("Begin to add the pod [%s] with ip address [%s]", pod.getName(), pod.getPodIP()));
                        byte[] json = mapper.writeValueAsBytes(pod);
                        client.prepareIndex(Const.K8S_POD_INDEX,Const.K8S_POD_TYPE).setSource(json, XContentType.JSON).get();
                        log.info(String.format("Add the pod [%s] with ip address [%s]", pod.getName(), pod.getPodIP()));
                    }else{
//                        log.info(String.format("The pod [%s] with ip address [%s] already exists!", pod.getName(), pod.getPodIP()));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //Update node info
    public void updateNodeInfo(){
        TransportClient client = myConfig.getESClient();
        log.info("===Update the node information===");
        List<NodeInfo> originNodes = esUtil.getStoredNodes();
        //Get current pods information
        GetNodesListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getNodesList",
                GetNodesListResponse.class);
        if(result.isStatus()){
            ObjectMapper mapper = new ObjectMapper();
            List<NodeInfo> currentNodes = result.getNodes();
            try{
                for(NodeInfo node : currentNodes){
                    if(!existInOrginNodes(node, originNodes)){
//                        log.info(String.format("Begin to add the node [%s] with ip address [%s]", node.getName(), node.getIp()));
                        byte[] json = mapper.writeValueAsBytes(node);
                        client.prepareIndex(Const.K8S_NODE_INDEX,Const.K8S_NODE_TYPE).setSource(json, XContentType.JSON).get();
                        log.info(String.format("Add the node [%s] with ip address [%s]", node.getName(), node.getIp()));
                    }else{
//                        log.info(String.format("The node [%s] with ip address [%s] already exists!", node.getName(), node.getIp()));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //Update trace status
    public void updateTraceStatus(){
        TransportClient client = myConfig.getESClient();

        log.info("===Update the trace status===");
        List<TraceStatus> originStatus = esUtil.getExistedTraceStatus();

        //Search the log generated after the previous timestamp

        QueryBuilder qb = QueryBuilders.existsQuery("RequestType");

        SearchResponse scrollResp = client.prepareSearch("logstash-*").setTypes("beats")
                .addSort("time", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setPostFilter(QueryBuilders.rangeQuery("time").from(prevTimestamp,false))
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits = new SearchHit[0];
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        try{
            while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                hits = scrollResp.getHits().getHits();
                log.info(String.format("The length of scroll relation search hits is [%d]", hits.length));
                String traceId;
                TraceStatus traceStatus;
                for (SearchHit hit : hits) {
                    //Handle the hit
                    map = hit.getSourceAsMap();
                    traceId = map.get("TraceId").toString();
                    boolean isExisted = false;
                    for(TraceStatus status : originStatus){
                        if(status.getTraceId().equals(traceId)){
                            isExisted = true;
                            break;
                        }
                    }
                    if(!isExisted){
                        traceStatus = new TraceStatus();
                        traceStatus.setTraceId(traceId);
                        traceStatus.setStatus(getStatusOfTrace(traceId));
                        //Store to the elasticsearch
                        byte[] json = mapper.writeValueAsBytes(traceStatus);
                        client.prepareIndex(Const.TRACE_STATUS_INDEX,Const.TRACE_STATUS_TYPE).setSource(json, XContentType.JSON).get();
                        originStatus.add(traceStatus);
                    }
                }

                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }

            //Update the prevTimestamp according to the last record
            if(hits.length > 0){
                SearchHit last = hits[hits.length - 1];
                map = last.getSourceAsMap();
                prevTimestamp = map.get("time").toString();
                log.info(String.format("Update the preTimestamp to [%s]",map.get("time").toString()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getStatusOfTrace(String traceId) {
        TransportClient client = myConfig.getESClient();
        QueryBuilder qb = QueryBuilders.termQuery("TraceId", traceId);

        SearchResponse scrollResp = client.prepareSearch(Const.LOGSTASH_LOG_INDEX).setTypes(Const.LOGSTASH_LOG_TYPE)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;
        Map<String, Object> map;

        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                if (map.get("log") != null) {
                    String log = map.get("log").toString();
                    if (log.contains("ExceptionMessage"))
                        return "false";
                }
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        return "true";
    }

    //Delete the useless span info
    public void deleteUselessSpanInfo(){
        TransportClient client = myConfig.getESClient();

        DeleteByQueryRequestBuilder deleteBuilder = DeleteByQueryAction.INSTANCE.newRequestBuilder(client);

        //Delete the mixer span records with the same trace id of istio-telemetry
        deleteBuilder
                .filter(QueryBuilders.matchQuery("localEndpoint.serviceName", "istio-telemetry"))
                .source(Const.ZIPKIN_SPAN_INDEX)
                .execute(new ActionListener<BulkByScrollResponse>() {
                    @Override
                    public void onResponse(BulkByScrollResponse response) {
                        long deleted = response.getDeleted();
                        log.info(String.format("Succeed to delete [%d] useless span record", deleted));
                    }
                    @Override
                    public void onFailure(Exception e) {
                        // Handle the exception
                        log.info(String.format("Fail to delete useless span info. With excepton message: [%s]", e.getStackTrace()));
                    }
                });

    }

    //Judge if the given pod exist in the origin pods list
    private boolean existInOrginPods(PodInfo pod, List<PodInfo> originPods){
        for(PodInfo originPod : originPods){
            if(pod.equals(originPod))
                return true;
        }
        return false;
    }

    //Judge if the given node exist in the origin nodes list
    private boolean existInOrginNodes(NodeInfo node, List<NodeInfo> originNodes){
        for(NodeInfo originNode : originNodes){
            if(node.equals(originNode))
                return true;
        }
        return false;
    }

}
