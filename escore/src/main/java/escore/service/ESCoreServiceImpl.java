package escore.service;

import escore.bean.*;
import escore.config.MyConfig;
import escore.init.InitIndexAndType;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;
import escore.response.QueryNodeInfoRes;
import escore.response.QueryPodInfoRes;
import escore.util.ESUtil;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
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
                List<TraceType> traceTypeList = getTraceTypesFromTraceList(requestType,traceInfoList);

                requestWithTraceInfo.setTraceTypeList(traceTypeList);
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

        GetRequestWithTraceIDRes res = new GetRequestWithTraceIDRes();
        res.setStatus(false);
        res.setMessage("This is the default message");
        List<RequestWithTraceInfo> requestWithTraceInfoList = new ArrayList<>();
        Map<String, Set<String>> requestWithTraceIdsMap = new HashMap<>();

        long endTimeValue = request.getEndTime();
        long lookback = request.getLookback();

        String beginTime = esUtil.convertTime(endTimeValue - lookback);
        String endTime = esUtil.convertTime(endTimeValue);

        log.info("===New method to get request with trace id by time range===");

        QueryBuilder qb = QueryBuilders.existsQuery("RequestType");

        SearchResponse scrollResp = client.prepareSearch("logstash-*").setTypes("beats")
                .addSort("time", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setPostFilter(QueryBuilders.rangeQuery("time")
                        .timeZone("+08:00")
                        .format("yyyy-MM-dd HH:mm:ss")
                        .from(beginTime,false)
                        .to(endTime,false))
                .setSize(100).get(); //max of 100 hits will be returned for each scroll

        //Scroll until no hits are returned
        //Construct the request type and its trace id set
        SearchHit[] hits;
        Map<String, Object> map;
        try{
            while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
                hits = scrollResp.getHits().getHits();
                String requestType,traceId;
                for (SearchHit hit : hits) {
                    //Handle the hit
                    map = hit.getSourceAsMap();
                    requestType = map.get("RequestType").toString();
                    traceId = map.get("TraceId").toString();
                    if(requestWithTraceIdsMap.get(requestType) != null){
                        requestWithTraceIdsMap.get(requestType).add(traceId);
                    }else{
                        Set<String> traceIds = new HashSet<>();
                        traceIds.add(traceId);
                        requestWithTraceIdsMap.put(requestType, traceIds);
                    }
                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        //Construct the return data
        for(String requestType : requestWithTraceIdsMap.keySet()){
            RequestWithTraceInfo requestWithTraceInfo = new RequestWithTraceInfo();
            requestWithTraceInfo.setRequestType(requestType);
            List<TraceInfo> traceInfoList = new ArrayList<>();
            for(String traceId : requestWithTraceIdsMap.get(requestType)){
                TraceInfo traceInfo = getTraceInfoById(traceId);
                traceInfoList.add(traceInfo);
            }
            List<TraceType> traceTypeList = getTraceTypesFromTraceList(requestType,traceInfoList);

            requestWithTraceInfo.setTraceTypeList(traceTypeList);
            requestWithTraceInfoList.add(requestWithTraceInfo);
        }

        //@deprecated Sort the request type
//        Collections.sort(requestWithTraceInfoList, new Comparator<RequestWithTraceInfo>() {
//            @Override
//            public int compare(RequestWithTraceInfo o1, RequestWithTraceInfo o2) {
//                return o1.getRequestType().compareTo(o2.getRequestType());
//            }
//        });

        //Sort the request type by lambda
        requestWithTraceInfoList.sort(Comparator.comparing(RequestWithTraceInfo::getRequestType));

        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the request with trace ids of specified time range. " +
                    "The size of request types is [%d].", requestWithTraceIdsMap.keySet().size()));
        res.setRequestWithTraceInfoList(requestWithTraceInfoList);

        return res;
    }

    //Retrive the trace types from the trace list
    private List<TraceType> getTraceTypesFromTraceList(String requestType, List<TraceInfo> traceInfoList) {
        Map<Set<String>, List<TraceInfo>> map = new HashMap<>();

        List<TraceType> traceTypeList = new ArrayList<>();

        int count = 1;

        for(TraceInfo traceInfo : traceInfoList){
            Set<String> serviceSet = traceInfo.getServiceName();
            if(map.get(serviceSet) == null){
                List<TraceInfo> traces = new ArrayList<>();
                traces.add(traceInfo);
                map.put(serviceSet, traces);
            }else{
                map.get(serviceSet).add(traceInfo);
            }
        }

        for(Set<String> serviceSet : map.keySet()){
            TraceType traceType = new TraceType();
            traceType.setTypeName(requestType + "-Type" + count);
            traceType.setTraceInfoList(map.get(serviceSet));
            traceType.setCount(map.get(serviceSet).size());
            count++;
            traceTypeList.add(traceType);
        }

        return traceTypeList;
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
        Set<String> serviceList = new LinkedHashSet<>();
        serviceList.add("istio-ingressgateway");

        QueryBuilder qb = QueryBuilders.matchQuery("traceId",traceId);
        SearchResponse scrollResp = client.prepareSearch(ZIPKIN_SPAN_INDEX).setTypes(ZIPKIN_SPAN_TYPE)
                .addSort("timestamp_millis", SortOrder.ASC)
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

}
