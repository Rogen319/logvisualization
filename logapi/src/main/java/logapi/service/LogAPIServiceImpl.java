package logapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import logapi.bean.*;
import logapi.response.GetLogByTraceIdRes;
import logapi.util.MyUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LogAPIServiceImpl implements LogAPIService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String LOGSTASH_LOG_INDEX = "logstash-*";
    private static final String LOGSTASH_LOG_TYPE = "beats";
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TransportClient client;

    @Autowired
    private PodService podService;

    @Autowired
    private NodeService nodeService;

    @Override
    public GetLogByTraceIdRes getLogByTraceId(String traceId) {
        GetLogByTraceIdRes res = new GetLogByTraceIdRes();

        List<LogItem> logs = getLogItemListByTraceId(traceId);
        res.setLogs(logs);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the trace log. Size is [%d].", logs.size()));

        return res;
    }

    //Get the LogItem list of the specified traceId
    private List<LogItem> getLogItemListByTraceId(String traceId){
        List<PodInfo> currentPods = podService.getCurrentPodInfo();
        List<NodeInfo> currentNodes = nodeService.getCurrentNodeInfo();

        List<LogItem> logItemList = new ArrayList<>();

        QueryBuilder qb = QueryBuilders.matchQuery("TraceId",traceId);

        SearchResponse scrollResp = client.prepareSearch(LOGSTASH_LOG_INDEX).setTypes(LOGSTASH_LOG_TYPE)
                .addSort("@timestamp", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;

        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
//                log.info("++++++======The hit is " + hit.getSourceAsString() + "======++++++");
                LogItem logItem = composeLogItemFromHit(hit, currentPods, currentNodes);
                logItemList.add(logItem);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        log.info(String.format("The length of corresponding logitem list is [%d]", logItemList.size()));
        return logItemList;
    }

    //Compose the LogItem from the SearchHit
    private LogItem composeLogItemFromHit(SearchHit hit, List<PodInfo> currentPods, List<NodeInfo> currentNodes){
        LogItem logItem = new LogItem();

        Map<String, Object> map = hit.getSourceAsMap();
        //Set timestamp
        if(map.get("@timestamp") != null){
            String timestamp = map.get("@timestamp").toString();
            logItem.setTimestamp(MyUtil.getLocalTimeFromUTCFormat(timestamp));
        }

        try{
            LogBean logBean = mapper.readValue(hit.getSourceAsString(), LogBean.class);
            logItem.setLogInfo(logBean.getLog());
            logItem.setLogType(logBean.getLogType());
            logItem.setRequestType(logBean.getRequestType());
            /**
             * Set trace information
             */
            TraceInfo traceInfo = new TraceInfo();
            traceInfo.setTraceId(logBean.getTraceId());
            traceInfo.setSpanId(logBean.getSpanId());
            traceInfo.setParentSpanId(logBean.getParentSpanId());

            logItem.setTraceInfo(traceInfo);
            /**
             * Set service information
             */
            ServiceInfo serviceInfo = new ServiceInfo();

            //Service name and instance information: name, container and version
            InstanceInfo instanceInfo = new InstanceInfo();
            String podName = logBean.getKubernetes().getPod().getName();
            instanceInfo.setInstanceName(podName);
            PodContainer container = new PodContainer();
            container.setName(logBean.getKubernetes().getContainer().getName());
            instanceInfo.setContainer(container);
            NodeInfo nodeInfo = podService.setInstanceInfo(serviceInfo, instanceInfo, podName, currentPods);

            serviceInfo.setInstanceInfo(instanceInfo);

            //Node information
            if(nodeInfo != null){
                nodeService.setNodeInfo(nodeInfo,currentNodes);
                serviceInfo.setNode(nodeInfo);
            }

            logItem.setServiceInfo(serviceInfo);
        }catch (Exception e){
            e.printStackTrace();
        }

        return logItem;
    }
}
