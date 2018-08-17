package escore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.*;
import escore.config.MyConfig;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.*;
import escore.util.Const;
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
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class ESCoreServiceImpl implements ESCoreService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MyConfig myConfig;

    @Autowired
    private ESUtil esUtil;

    @Autowired
    private PodService podService;

    //Just for demo
    @Override
    public String demo() {
        TransportClient client = myConfig.getESClient();

        StringBuilder sb = new StringBuilder();

        MatchQueryBuilder builder = QueryBuilders.matchQuery("kubernetes.container.name", "ts-login-service");
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

//        log.info(String.format("===Begin time is [%s], endTime is [%s]===", beginTime, endTime));

        beginTime = beginTime.split(" ")[0] + " 00:00:00";
        endTime = endTime.split(" ")[0] + " 23:59:59";

//        log.info(String.format("===Final begin time is [%s], endTime is [%s]===", beginTime, endTime));

        log.info("===New method to get request with trace id by time range===");

        QueryBuilder qb = QueryBuilders.existsQuery("RequestType");

        SearchResponse scrollResp = client.prepareSearch("logstash-*").setTypes("beats")
                .addSort("time", SortOrder.DESC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setPostFilter(QueryBuilders.rangeQuery("time")
                        .timeZone("+08:00")
                        .format("yyyy-MM-dd HH:mm:ss")
                        .from(beginTime, false)
                        .to(endTime, false))
                .setSize(100).get(); //max of 100 hits will be returned for each scroll

        //Scroll until no hits are returned
        //Construct the request type and its trace id set
        SearchHit[] hits;
        Map<String, Object> map;
        try {
            while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
                hits = scrollResp.getHits().getHits();
                String requestType, traceId;
                for (SearchHit hit : hits) {
                    //Handle the hit
                    map = hit.getSourceAsMap();
                    requestType = map.get("RequestType").toString();
                    traceId = map.get("TraceId").toString();
                    if (requestWithTraceIdsMap.get(requestType) != null) {
                        requestWithTraceIdsMap.get(requestType).add(traceId);
                    } else {
                        Set<String> traceIds = new HashSet<>();
                        traceIds.add(traceId);
                        requestWithTraceIdsMap.put(requestType, traceIds);
                    }
                }
                scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Construct the return data
        for (String requestType : requestWithTraceIdsMap.keySet()) {

            RequestWithTraceInfo requestWithTraceInfo = new RequestWithTraceInfo();
            requestWithTraceInfo.setRequestType(requestType);
            List<TraceInfo> traceInfoList = new ArrayList<>();
            for (String traceId : requestWithTraceIdsMap.get(requestType)) {
                TraceInfo traceInfo = getTraceInfoById(traceId);

                //Set the status of trace
                String statusString = esUtil.getStatusOfTrace(traceId);
                if(statusString.equals("true"))
                    traceInfo.setStatus(Const.NORMAL_TRACE_FLAG);
                else
                    traceInfo.setStatus(Const.ERROR_TRACE_FLAG);

                //Set the count of three kind of log
                setCountOfTraceInfo(traceInfo);
                traceInfoList.add(traceInfo);
            }
            List<TraceType> traceTypeList = getTraceTypesFromTraceList(requestType, traceInfoList);
            requestWithTraceInfo.setTraceTypeList(traceTypeList);
            //Judge if the trace only contains 1 service
            boolean flag = false;
            for(TraceType traceType : traceTypeList){
                if(traceType.getTraceInfoList().get(0).getServiceList().size() > 1){
                    flag = true;
                    break;
                }
            }
            if(flag){
                int normalCount = 0, errorCount = 0, exceptionCount = 0;
                int normalTraceCount = 0, errorTraceCount = 0;
                for (TraceType traceType : traceTypeList) {
                    normalCount += traceType.getNormalCount();
                    errorCount += traceType.getErrorCount();
                    exceptionCount += traceType.getExceptionCount();
                    normalTraceCount += traceType.getNormalTraceCount();
                    errorTraceCount += traceType.getErrorTraceCount();
                }
                requestWithTraceInfo.setNormalCount(normalCount);
                requestWithTraceInfo.setErrorCount(errorCount);
                requestWithTraceInfo.setExceptionCount(exceptionCount);
                requestWithTraceInfo.setNormalTraceCount(normalTraceCount);
                requestWithTraceInfo.setErrorTraceCount(errorTraceCount);

                requestWithTraceInfoList.add(requestWithTraceInfo);
            }

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

    @Override
    public GetInstanceNamesFromESRes getInstanceNamesOfSpecifiedService(String serviceName) {
        GetInstanceNamesFromESRes res = new GetInstanceNamesFromESRes();

        List<String> instanceNames = new ArrayList<>();

        TransportClient client = myConfig.getESClient();
        QueryBuilder qb = QueryBuilders.termQuery("serviceName", serviceName);

        SearchResponse scrollResp = client.prepareSearch(Const.K8S_POD_INDEX).setTypes(Const.K8S_POD_TYPE)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;
        Map<String, Object> map;

        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                map = hit.getSourceAsMap();
                if (map.get("name") != null) {
                    instanceNames.add(map.get("name").toString());
                }
            }
        }

        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the instance names of service:[%s]. Size is [%d].", serviceName, instanceNames.size()));
        res.setInstanceNames(instanceNames);

        return res;
    }

    //Set the count of three kind log
    private void setCountOfTraceInfo(TraceInfo traceInfo) {
        String traceId = traceInfo.getTraceId();
        TransportClient client = myConfig.getESClient();
        QueryBuilder qb = QueryBuilders.termQuery("TraceId", traceId);

        SearchResponse scrollResp = client.prepareSearch(Const.LOGSTASH_LOG_INDEX).setTypes(Const.LOGSTASH_LOG_TYPE)
                .addSort("time", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;
        Map<String, Object> map;

        int normalCount = 0, errorCount = 0, exceptionCount = 0;

        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                if (map.get("log") != null) {
                    String log = map.get("log").toString();
                    if (log.contains("ExceptionMessage")) {
                        if (log.contains("Error") || log.contains("error"))
                            errorCount++;
                        else
                            exceptionCount++;
                    } else {
                        normalCount++;
                    }
                }

                //待删除
                if(Math.random() < 0.2)
                    errorCount++;
                else
                    normalCount++;
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        traceInfo.setNormalCount(normalCount);
        traceInfo.setErrorCount(errorCount);
        traceInfo.setExceptionCount(exceptionCount);
    }

    //Retrive the trace types from the trace list
    private List<TraceType> getTraceTypesFromTraceList(String requestType, List<TraceInfo> traceInfoList) {
        Map<Set<String>, List<TraceInfo>> map = new HashMap<>();

        List<TraceType> traceTypeList = new ArrayList<>();

        int count = 1;

        for (TraceInfo traceInfo : traceInfoList) {
            Set<String> serviceSet = traceInfo.getServiceList();
            if (map.get(serviceSet) == null) {
                List<TraceInfo> traces = new ArrayList<>();
                traces.add(traceInfo);
                map.put(serviceSet, traces);
            } else {
                map.get(serviceSet).add(traceInfo);
            }
        }

        int normalCount, errorCount, exceptionCount;
        int normalTraceCount, errorTraceCount;
        for (Set<String> serviceSet : map.keySet()) {
            TraceType traceType = new TraceType();
            traceType.setTypeName(requestType + "-Type" + count);
            List<TraceInfo> traceInfos = map.get(serviceSet);
            traceType.setTraceInfoList(traceInfos);
            traceType.setCount(map.get(serviceSet).size());
            //Count the number of three kind log
            normalCount = 0;
            errorCount = 0;
            exceptionCount = 0;
            normalTraceCount = 0;
            errorTraceCount = 0;
            for (TraceInfo traceInfo : traceInfos) {
                normalCount += traceInfo.getNormalCount();
                errorCount += traceInfo.getErrorCount();
                exceptionCount += traceInfo.getExceptionCount();
                if(traceInfo.getStatus() == Const.NORMAL_TRACE_FLAG)
                    normalTraceCount++;
                else
                    errorTraceCount++;
            }
            traceType.setNormalCount(normalCount);
            traceType.setErrorCount(errorCount);
            traceType.setExceptionCount(exceptionCount);
            traceType.setNormalTraceCount(normalTraceCount);
            traceType.setErrorTraceCount(errorTraceCount);

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
        for (PodInfo podInfo : storedPods) {
            if (podInfo.getName().equals(podName)) {
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
        for (NodeInfo node : storedNodes) {
            if (node.equals(nodeInfo)) {
                res.setStatus(true);
                res.setMessage("Succeed to get node information in the stored list(es)");
                res.setNodeInfo(node);
                return res;
            }
        }
        return res;
    }

    //Get the trace info by the trace id
    private TraceInfo getTraceInfoById(String traceId) {
        TransportClient client = myConfig.getESClient();

        TraceInfo res = new TraceInfo();
        res.setTraceId(traceId);

        //Get the service name
        Set<String> serviceList = new LinkedHashSet<>();

        QueryBuilder qb = QueryBuilders.matchQuery("traceId", traceId);
        SearchResponse scrollResp = client.prepareSearch(Const.ZIPKIN_SPAN_INDEX).setTypes(Const.ZIPKIN_SPAN_TYPE)
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
        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
//            log.info(String.format("The length of scroll span with traceId:[%s] search hits is [%d]", traceId, hits.length));
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                endpoint = map.get("localEndpoint") != null ? map.get("localEndpoint").toString() : "";
                if (!endpoint.equals("")) {
//                    log.info(String.format("The localEndpoint is %s", endpoint));
                    matcher = pattern.matcher(endpoint);
                    if (matcher.find()) {
//                        log.info(String.format("Matcher group 1 is %s", matcher.group(1)));
                        serviceList.add(matcher.group(1));
                    } else {
                        matcher = pattern2.matcher(endpoint);
                        if (matcher.find()) {
//                            log.info(String.format("Matcher group 1 is %s", matcher.group(1)));
                            serviceList.add(matcher.group(1));
                        }
                    }
                }
                endpoint = "";
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }

        List<ServiceWithCount> services = new ArrayList<>();
        Map<String, ServiceWithCount> serviceMap = new HashMap<>();

        for (String serviceName : serviceList) {
            ServiceWithCount serviceWithCount = new ServiceWithCount();
            serviceWithCount.setServiceName(serviceName);
            serviceMap.put(serviceName, serviceWithCount);
        }

        //Get all of the logs with specified traceid
        List<SimpleLog> logs = getLogListByCondition("TraceId", traceId);

        for (SimpleLog log : logs) {
            ServiceWithCount serviceWithCount = serviceMap.get(log.getServiceName());
            if (serviceWithCount != null) {
                if (log.getIsError() == 0)
                    serviceWithCount.setNormalCount(serviceWithCount.getNormalCount() + 1);
                else if (log.getIsError() == 2)
                    serviceWithCount.setExceptionCount(serviceWithCount.getExceptionCount() + 1);
                else
                    serviceWithCount.setErrorCount(serviceWithCount.getErrorCount() + 1);
            }
        }

        for (String serviceName : serviceMap.keySet()) {
            services.add(serviceMap.get(serviceName));
        }
        res.setServiceList(serviceList);
        res.setServiceWithCounts(services);

        return res;
    }

    //Get the log list by condition
    private List<SimpleLog> getLogListByCondition(String termName, String termValue) {
        TransportClient client = myConfig.getESClient();

        List<PodInfo> currentPods = podService.getCurrentPodInfo();

        List<SimpleLog> logItemList = new ArrayList<>();

        QueryBuilder qb = QueryBuilders.termQuery(termName, termValue);

        SearchResponse scrollResp = client.prepareSearch(Const.LOGSTASH_LOG_INDEX).setTypes(Const.LOGSTASH_LOG_TYPE)
                .addSort("time", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;

        while (scrollResp.getHits().getHits().length != 0) { // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
                SimpleLog logItem = composeLogItemFromHit(hit, currentPods);
                logItemList.add(logItem);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        log.info(String.format("The length of corresponding logitem list is [%d]", logItemList.size()));
        return logItemList;
    }

    private SimpleLog composeLogItemFromHit(SearchHit hit, List<PodInfo> currentPods) {
        SimpleLog log = new SimpleLog();

        //Default, it is not an error
        log.setIsError(0);
        try {
            LogBean logBean = mapper.readValue(hit.getSourceAsString(), LogBean.class);
            String podName = logBean.getKubernetes().getPod().getName();
            String serviceName = podService.getServiceName(podName, currentPods);
            log.setServiceName(serviceName);
            String logType = logBean.getLogType();
            if (logType.equals("InternalMethod")) {
                Map<String, Object> map = hit.getSourceAsMap();
                if (map.get("Content") == null) {
                    String logInfo = String.format("[ExceptionMessage:%s][ExceptionCause:%s][ExceptionStack:%s]",
                            map.get("ExceptionMessage") != null ? map.get("ExceptionMessage").toString() : "",
                            map.get("ExceptionCause") != null ? map.get("ExceptionCause").toString() : "",
                            map.get("ExceptionStack") != null ? map.get("ExceptionStack").toString() : "");
                    log.setIsError(2);
                    if (logInfo.contains("error") || logInfo.contains("Error")) {
                        log.setIsError(1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //待删除
        if(Math.random() < 0.33){
            log.setIsError(0);
        }else if(Math.random() < 0.66){
            log.setIsError(1);
        }else{
            log.setIsError(2);
        }

        return log;
    }

    @Override
    public List<TraceSequenceRes> getSequenceInfo() {
        TransportClient client = myConfig.getESClient();
        QueryBuilder qb = QueryBuilders.existsQuery("RequestType");

        Map<String, Set<String>> reqTypeTraId = new HashMap<>();

        SearchResponse ret = client.prepareSearch(Const.LOGSTASH_LOG_INDEX)
                .setTypes(Const.LOGSTASH_LOG_TYPE)
                .setQuery(qb)
                .setScroll(new TimeValue(60000))
                .setSize(100)
                .execute().actionGet();

        while (null != ret && 0 != ret.getHits().getHits().length) {
            SearchHit[] hits = ret.getHits().getHits();
            Stream<SearchHit> hitStream = Stream.of(hits);

            hitStream.forEach(n -> {
                String requestType = n.getSourceAsMap().get("RequestType").toString();
                String traceId = n.getSourceAsMap().get("TraceId").toString();

                if (reqTypeTraId.containsKey(requestType)) {
                    reqTypeTraId.get(requestType).add(traceId);
                } else {
                    Set<String> set = new HashSet<>();
                    set.add(traceId);
                    reqTypeTraId.put(requestType, set);
                }
            });

            ret = client.prepareSearchScroll(
                    ret.getScrollId()).setScroll(new TimeValue(60000))
                    .execute().actionGet();
        }


        reqTypeTraId.forEach((k, v) ->
                log.info(k + ": " + v.toString()));


        RestTemplate restTemplate = new RestTemplate();

        Comparator<LogItem> logItemComparator = (o1, o2) -> {

            if (o1.getTimestamp().equals(o2.getTimestamp())) {
                if (o1.getLogType().equals("InvocationRequest")) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return o1.getTimestamp().compareTo(o2.getTimestamp());

        };

        List<TraceSequenceRes> traceSequenceRes = new ArrayList<>();

        reqTypeTraId.forEach((k, v) -> {

            List<List<String>> seqs = new ArrayList<>();
            List<SequenceInfo> sequenceInfos = new ArrayList<>();
            for (String traceId : v) {
                StringBuilder sb =
                        new StringBuilder("http://10.141.212.25:16319/getLogByTraceId/");
                sb.append(traceId);
                sb.append("/0");
                LogResponse logResp =
                        restTemplate.getForObject(sb.toString(), LogResponse.class);
                List<LogItem> logItems = logResp.getLogs();
                LogItem[] logItemArr = logItems.toArray(new LogItem[logItems.size()]);
                Arrays.sort(logItemArr, logItemComparator);

                List<String> svcSeq = new ArrayList<>();
                Stream<LogItem> logItemStream = Stream.of(logItemArr);
                logItemStream.forEach(n -> {
                    if (n.getLogType().equals("InvocationRequest") || n.getLogType().equals("InvocationResponse"))
                        svcSeq.add(n.getServiceInfo().getServiceName());
                });

                log.info("{}-{}: {}", k, traceId, svcSeq.toString());


                if (!seqs.contains(svcSeq)){
                    SequenceInfo sequenceInfo = new SequenceInfo();
                    sequenceInfo.setTraceId(traceId);
                    sequenceInfo.setServiceSequence(svcSeq);
                    sequenceInfos.add(sequenceInfo);
                }
                seqs.add(svcSeq);


            }

            TraceSequenceRes tsr = new TraceSequenceRes();
            tsr.setRequestType(k);
            tsr.setSequenceCount(sequenceInfos.size());
            tsr.setSequenceInfos(sequenceInfos);

            traceSequenceRes.add(tsr);

        });


        return traceSequenceRes;
    }

}
