package escore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.*;
import escore.config.MyConfig;
import escore.init.InitIndexAndType;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;
import escore.response.QueryNodeInfoRes;
import escore.response.QueryPodInfoRes;
import escore.util.ESUtil;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ESCoreServiceImpl implements ESCoreService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String ZIPKIN_SPAN_INDEX = "zipkin:span-*";
    private static final String ZIPKIN_SPAN_TYPE = "span";
    private static final String TEMP_RELATION_INDEX = "temp_rt_relation";
    private static final String TEMP_RELATION_TYPE = "relation";

    @Autowired
    private MyConfig myConfig;

    @Autowired
    private ESUtil esUtil;

    //Just for demo
    @Override
    public String demo() {
        TransportClient client = myConfig.getESClient();

        StringBuilder sb = new StringBuilder();

        MatchQueryBuilder builder = QueryBuilders.matchQuery("kubernetes.container.name","ts-login-service");
        SearchResponse response = client.prepareSearch("logstash-*").setTypes("beats").setQuery(builder).setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        log.info(String.format("The length of hits is [%d]", hits.length));
        for (SearchHit hit : hits) {
            log.info(hit.getSourceAsString());
            sb.append(hit.getSourceAsString());
        }
        client.close();
        return sb.toString();
    }

    //Get the request types list
    private GetRequestTypesRes getRequestTypes(String index, String type) {
        log.info(String.format("Get reuqest types from [%s:%s]", index, type));
        TransportClient client = myConfig.getESClient();

        GetRequestTypesRes res = new GetRequestTypesRes();
        res.setStatus(false);
        res.setMessage("This is the default message!");

        SearchResponse scrollResp = client.prepareSearch(index).setTypes(type)
                .setScroll(new TimeValue(60000))
                .setSize(100).get();

        //Scroll until no hits are returned
        SearchHit[] hits;
        Set<String> requestTypes = new HashSet<>();
        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            log.info(String.format("The length of scroll request type search hits is [%d]", hits.length));
            Map<String, Object> map;
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                requestTypes.add(map.get("requestType").toString());
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        log.info(String.format("Succeed to get request types. There are [%d] request types now.", requestTypes.size()));
        res.setRequestTypes(requestTypes);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get request types. There are [%d] request types now.", requestTypes.size()));

        return res;
    }

    //Get the request type with its trace ids
    @Override
    public GetRequestWithTraceIDRes getRequestWithTraceID() {
        TransportClient client = myConfig.getESClient();

        GetRequestWithTraceIDRes res = new GetRequestWithTraceIDRes();
        res.setStatus(false);
        res.setMessage("This is the default message");
        List<RequestWithTraceInfo> requestWithTraceInfoList = new ArrayList<>();

        //Get request types first
        GetRequestTypesRes getRequestTypesRes = this.getRequestTypes(InitIndexAndType.REQUEST_TRACE_RELATION_INDEX, "relation");
        if(getRequestTypesRes.isStatus()){
            MatchQueryBuilder qb;
            SearchResponse scrollResp;
            SearchHit[] hits;
            Map<String, Object> map;
            for(String requestType : getRequestTypesRes.getRequestTypes()){
                //Create the object to store the information
                RequestWithTraceInfo requestWithTraceInfo = new RequestWithTraceInfo();
                requestWithTraceInfo.setRequestType(requestType);
                List<TraceInfo> traceInfoList = new ArrayList<>();

                qb = QueryBuilders.matchQuery("requestType",requestType);
                scrollResp = client.prepareSearch(InitIndexAndType.REQUEST_TRACE_RELATION_INDEX).setTypes("relation")
                        .setScroll(new TimeValue(60000))
                        .setQuery(qb)
                        .setSize(100).get();
                while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                    hits = scrollResp.getHits().getHits();
                    log.info(String.format("The length of scroll requestType:[%s] search hits is [%d]", requestType, hits.length));
                    for (SearchHit hit : hits) {
                        //Handle the hit
                        map = hit.getSourceAsMap();
                        TraceInfo traceInfo = getTraceInfoById(map.get("traceId").toString());
                        traceInfoList.add(traceInfo);
                    }
                    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
                }
                requestWithTraceInfo.setTraceInfoList(traceInfoList);
                requestWithTraceInfo.setCount(traceInfoList.size());
                requestWithTraceInfoList.add(requestWithTraceInfo);
            }
            res.setStatus(true);
            res.setMessage(String.format("Succeed to get the request with trace ids. The size of request types is [%d].", getRequestTypesRes.getRequestTypes().size()));
        }
        res.setRequestWithTraceInfoList(requestWithTraceInfoList);

        return res;
    }

    @Override
    public GetRequestWithTraceIDRes getRequestWithTraceIDByTimeRange(GetRequestWithTraceIDByTimeRangeReq request) {
        TransportClient client = myConfig.getESClient();

        //Store the relation information in the time range
        storeTheRelationTemp(request.getEndTime(), request.getLookback());

        GetRequestWithTraceIDRes res = new GetRequestWithTraceIDRes();
        res.setStatus(false);
        res.setMessage("This is the default message");
        List<RequestWithTraceInfo> requestWithTraceInfoList = new ArrayList<>();

        //Get request types first
        GetRequestTypesRes getRequestTypesRes = this.getRequestTypes(TEMP_RELATION_INDEX, TEMP_RELATION_TYPE);
        if(getRequestTypesRes.isStatus()){
            MatchQueryBuilder qb;
            SearchResponse scrollResp;
            SearchHit[] hits;
            Map<String, Object> map;
            for(String requestType : getRequestTypesRes.getRequestTypes()){
                //Create the object to store the information
                RequestWithTraceInfo requestWithTraceInfo = new RequestWithTraceInfo();
                requestWithTraceInfo.setRequestType(requestType);
                List<TraceInfo> traceInfoList = new ArrayList<>();

                qb = QueryBuilders.matchQuery("requestType",requestType);
                scrollResp = client.prepareSearch(TEMP_RELATION_INDEX).setTypes(TEMP_RELATION_TYPE)
                        .setScroll(new TimeValue(60000))
                        .setQuery(qb)
                        .setSize(100).get();
                while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                    hits = scrollResp.getHits().getHits();
                    log.info(String.format("The length of scroll requestType:[%s] search hits is [%d]", requestType, hits.length));
                    for (SearchHit hit : hits) {
                        //Handle the hit
                        map = hit.getSourceAsMap();
                        TraceInfo traceInfo = getTraceInfoById(map.get("traceId").toString());
                        traceInfoList.add(traceInfo);
                    }
                    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
                }
                requestWithTraceInfo.setTraceInfoList(traceInfoList);
                requestWithTraceInfo.setCount(traceInfoList.size());
                requestWithTraceInfoList.add(requestWithTraceInfo);
            }
            res.setStatus(true);
            res.setMessage(String.format("Succeed to get the request with trace ids of specified time range. " +
                    "The size of request types is [%d].", getRequestTypesRes.getRequestTypes().size()));
        }
        res.setRequestWithTraceInfoList(requestWithTraceInfoList);

        deleteTempIndex(client);

        return res;
    }

    @Override
    public QueryPodInfoRes queryPodInfo(String podName) {
        QueryPodInfoRes res = new QueryPodInfoRes();
        res.setStatus(false);
        res.setMessage(String.format("No pod named [%s]", podName));
        res.setPodInfo(null);

        List<PodInfo> storedPods = esUtil.getStoredPods();
        for(PodInfo podInfo : storedPods){
            if(podInfo.getName().equals(podName)){
                res.setPodInfo(podInfo);
                res.setStatus(true);
                res.setMessage("Succeed to get pod information in the stored list(es)");
                return res;
            }
        }
        return res;
    }

    @Override
    public QueryNodeInfoRes queryNodeInfo(NodeInfo nodeInfo) {
        QueryNodeInfoRes res = new QueryNodeInfoRes();
        res.setStatus(false);
        res.setMessage(String.format("No node named [%s] with ip:[%s]", nodeInfo.getName(), nodeInfo.getIp()));
        res.setNodeInfo(null);

        List<NodeInfo> storedNodes = esUtil.getStoredNodes();
        for(NodeInfo node : storedNodes){
            if(node.equals(nodeInfo)){
                res.setStatus(true);
                res.setMessage("Succeed to get node information in the stored list(es)");
                res.setNodeInfo(node);
                return res;
            }
        }
        return res;
    }

    //Get the trace info by the trace id
    private TraceInfo getTraceInfoById(String traceId){
        TransportClient client = myConfig.getESClient();

        TraceInfo res = new TraceInfo();
        res.setTraceId(traceId);

        //Get the service name
        Set<String> serviceList = new HashSet<>();
        QueryBuilder qb = QueryBuilders.matchQuery("traceId",traceId);
        SearchResponse scrollResp = client.prepareSearch(ZIPKIN_SPAN_INDEX).setTypes(ZIPKIN_SPAN_TYPE)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get();
        SearchHit[] hits;
        Map<String, Object> map;
        String endpoint = "";
        //To get the service name from the endpoint
        Pattern pattern = Pattern.compile("serviceName=(.*)?,");
        Pattern pattern2 = Pattern.compile("serviceName=(.*)?}");
        Matcher matcher;
        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            log.info(String.format("The length of scroll span with traceId:[%s] search hits is [%d]", traceId, hits.length));
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                endpoint = map.get("localEndpoint") != null ? map.get("localEndpoint").toString() : "";
                if(!endpoint.equals("")){
//                    log.info(String.format("The localEndpoint is %s", endpoint));
                    matcher = pattern.matcher(endpoint);
                    if(matcher.find()){
//                        log.info(String.format("Matcher group 1 is %s", matcher.group(1)));
                        serviceList.add(matcher.group(1));
                    }else{
                        matcher = pattern2.matcher(endpoint);
                        if(matcher.find()){
//                            log.info(String.format("Matcher group 1 is %s", matcher.group(1)));
                            serviceList.add(matcher.group(1));
                        }
                    }
                }
                endpoint = "";
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        res.setServiceName(serviceList);

        return res;
    }

    //Store the relation information temporary
    private void storeTheRelationTemp(long endTimeValue, long lookback){
        TransportClient client = myConfig.getESClient();

        createTempIndex(client);

        String beginTime = esUtil.convertTime(endTimeValue - lookback);
        String endTime = esUtil.convertTime(endTimeValue);

        log.info("===Store the relation information temporary===");

        QueryBuilder qb = QueryBuilders.existsQuery("RequestType");

        SearchResponse scrollResp = client.prepareSearch("logstash-*").setTypes("beats")
                .addSort("@timestamp", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setPostFilter(QueryBuilders.rangeQuery("@timestamp")
                        .timeZone("+08:00")
                        .format("yyyy-MM-dd HH:mm:ss")
                        .from(beginTime,false)
                        .to(endTime,false))
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        List<RTRelation> existedRelations = new ArrayList<>();

        SearchHit[] hits;
        Map<String, Object> map;
        ObjectMapper mapper = new ObjectMapper();
        int count = 0;
        try{
            while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                hits = scrollResp.getHits().getHits();
//                log.info(String.format("The length of scroll relation search hits is [%d]", hits.length));
                String requestType,traceId;
                RTRelation relation;
                for (SearchHit hit : hits) {
                    //Handle the hit
                    map = hit.getSourceAsMap();
                    requestType = map.get("RequestType").toString();
                    traceId = map.get("TraceId").toString();
                    relation = new RTRelation();
                    relation.setRequestType(requestType);
                    relation.setTraceId(traceId);
                    if(!existInExistedRelation(relation, existedRelations)){
                        byte[] json = mapper.writeValueAsBytes(relation);
                        //Set the mode to be synchronous
                        client.prepareIndex(TEMP_RELATION_INDEX,TEMP_RELATION_TYPE)
                                .setSource(json, XContentType.JSON)
                                .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL)
                                .get();
                        count++;
                        existedRelations.add(relation);
                    }
                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        log.info(String.format("Store [%d] relation records ", count));
    }

    private void createTempIndex(TransportClient client){
        IndicesAdminClient indicesAdminClient = client.admin().indices();

        CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(TEMP_RELATION_INDEX)
                .execute().actionGet();
        if (createIndexResponse.isAcknowledged()) {
            log.info(String.format("Index [%s] has been created successfully!", TEMP_RELATION_INDEX));

            //Add request trace relation type
            addRelationType(indicesAdminClient);
        } else {
            log.info(String.format("Fail to create index [%s]!", TEMP_RELATION_INDEX));
        }
    }

    private void deleteTempIndex(TransportClient client){
        IndicesAdminClient indicesAdminClient = client.admin().indices();
        indicesAdminClient.prepareDelete(TEMP_RELATION_INDEX).execute().actionGet();
    }

    //Add relation type in the rt_relation index
    private void addRelationType(IndicesAdminClient indicesAdminClient){
        indicesAdminClient.preparePutMapping(TEMP_RELATION_INDEX)
                .setType("relation")
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"requestType\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    },\n" +
                        "    \"traceId\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}", XContentType.JSON)
                .get();
    }

    //Judge if the given relation exists in the original relation list
    private boolean existInExistedRelation(RTRelation relation, List<RTRelation> originRelations){
        for(RTRelation originRelation : originRelations){
            if(relation.equals(originRelation))
                return true;
        }
        return false;
    }
}
