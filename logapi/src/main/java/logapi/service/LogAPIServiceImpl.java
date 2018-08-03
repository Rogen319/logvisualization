package logapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import logapi.bean.*;
import logapi.request.GetLogByInstanceNameAndTraceIdReq;
import logapi.response.GetLogByServiceNameAndTraceIdRes;
import logapi.response.LogResponse;
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

import java.io.IOException;
import java.util.*;

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

    //Get log by trace id
    @Override
    public LogResponse getLogByTraceId(String traceId, int flag) {
        LogResponse res = new LogResponse();

        List<LogItem> logs = getLogItemListByCondition("TraceId", traceId);

        //flag:1 represents order by service/uri
        if(flag == 0){
            res.setLogs(logs);
        }
        else{
            //Sort the uri by time
            Set<String> requestUri = new LinkedHashSet<>();
            for(LogItem log : logs){
                if(log.getLogType().equals("InvocationRequest")){
                    requestUri.add(log.getUri());
                }
            }

            List<LogItem> result = new ArrayList<>();
            //Add the log according to the uri
            for(String uri : requestUri){
                for(LogItem log : logs){
                    if(log.getUri().equals(uri)){
                        result.add(log);
                    }
                }
            }
            res.setLogs(result);
        }


        //Sort by uri, then time and last type
//        Collections.sort(result, new Comparator<LogItem>() {
//            @Override
//            public int compare(LogItem o1, LogItem o2) {
//                String t1 = o1.getTimestamp();
//                String t2 = o2.getTimestamp();
//                if(t1.compareTo(t2) == 0){
//                    if(o1.getLogType().equals("InvocationRequest")){
//                        return -1;
//                    }else if(o1.getLogType().equals("InvocationResponse")){
//                        return 1;
//                    }else{
//                        if(o2.getLogType().equals("InvocationRequest"))
//                            return 1;
//                        else if(o2.getLogType().equals("InvocationResponse"))
//                            return -1;
//                        else
//                            return 0;
//                    }
//                }
//                return t1.compareTo(t2);
//            }
//        });

        //Set the error count
        int normalCount = 0;
        int errorCount = 0;
        int exceptionCount = 0;
        for(LogItem log : logs){
            int f = log.getIsError();
            if(f == 0)
                normalCount++;
            else if(f == 1)
                errorCount++;
            else
                exceptionCount++;
        }
        res.setNormalCount(normalCount);
        res.setErrorCount(errorCount);
        res.setExceptionCount(exceptionCount);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the trace log. Size is [%d].", logs.size()));

        return res;
    }

    //Get log by instance name
    @Override
    public LogResponse getLogByInstanceName(String instanceName) {
        LogResponse res = new LogResponse();

        List<LogItem> logs = getLogItemListByCondition("kubernetes.pod.name.keyword",instanceName);

        //Set the error count
        int normalCount = 0;
        int errorCount = 0;
        int exceptionCount = 0;
        for(LogItem log : logs){
            int flag = log.getIsError();
            if(flag == 0)
                normalCount++;
            else if(flag == 1)
                errorCount++;
            else
                exceptionCount++;
        }

        res.setLogs(logs);
        res.setNormalCount(normalCount);
        res.setErrorCount(errorCount);
        res.setExceptionCount(exceptionCount);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the instance log. Size is [%d].", logs.size()));

        return res;
    }

    @Override
    public GetLogByServiceNameAndTraceIdRes getLogByServiceNameAndTraceId(GetLogByInstanceNameAndTraceIdReq request) {
        GetLogByServiceNameAndTraceIdRes res = new GetLogByServiceNameAndTraceIdRes();

        List<InstanceAndTraceIdLog> instanceAndTraceIdLogList = new ArrayList<>();
        //Get all of the instances of the specified service
        List<String> instanceNames = podService.getInstanceOfService(request.getServiceName());
        for(String instanceName : instanceNames){
            InstanceAndTraceIdLog instanceAndTraceIdLog = getSpecifiedInstanceLog(instanceName, request.getTraceId());
            if(instanceAndTraceIdLog.getLogs().size() > 0){
                instanceAndTraceIdLogList.add(instanceAndTraceIdLog);
            }
        }
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get all of the logs of instances belong to the specified service:[%s].The size is [%d].",
                request.getServiceName(), instanceAndTraceIdLogList.size()));
        res.setInstanceWithLogList(instanceAndTraceIdLogList);

        return res;
    }

    //Get the log information of specified service instance and trace id
    private InstanceAndTraceIdLog getSpecifiedInstanceLog(String instanceName, String traceId){
        InstanceAndTraceIdLog res = new InstanceAndTraceIdLog();

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("kubernetes.pod.name.keyword",instanceName))
                .must(QueryBuilders.termQuery("TraceId",traceId));

        List<BasicLogItem> logs = new ArrayList<>();
        SearchResponse scrollResp = client.prepareSearch(LOGSTASH_LOG_INDEX).setTypes(LOGSTASH_LOG_TYPE)
                .addSort("time", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits = new SearchHit[0];
        Map<String, Object> map;

        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                BasicLogItem log = new BasicLogItem();
                if(map.get("time") != null){
                    String timestamp = map.get("time").toString();
                    timestamp = timestamp.substring(0,23) + "Z";
                    log.setTimestamp(MyUtil.getLocalTimeFromUTCFormat(timestamp));
                }
                if(map.get("LogType") != null){
                    String logType = map.get("LogType").toString();
                    log.setLogType(logType);
                    //Set the log information according to the log type
                    setLogInfo(log, logType, map);
                }
                if(map.get("RequestType") != null){
                    log.setRequestType(map.get("RequestType").toString());
                }
                if(map.get("URI") != null){
                    log.setUri(map.get("URI").toString());
                }
                logs.add(log);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        log.info(String.format("The length of corresponding logitem list is [%d]", logs.size()));

        //Set the error count
        int normalCount = 0;
        int errorCount = 0;
        int exceptionCount = 0;
        for(BasicLogItem log : logs){
            int flag = log.getIsError();
            if(flag == 0)
                normalCount++;
            else if(flag == 1)
                errorCount++;
            else
                exceptionCount++;
        }
        res.setLogs(logs);
        res.setNormalCount(normalCount);
        res.setErrorCount(errorCount);
        res.setExceptionCount(exceptionCount);
        if(hits.length > 0){
            SearchHit hit = hits[hits.length - 1];
            try {
                LogBean logBean = mapper.readValue(hit.getSourceAsString(), LogBean.class);

                /**
                 * Set service information
                 */
                ServiceInfo serviceInfo = new ServiceInfo();

                List<PodInfo> currentPods = podService.getCurrentPodInfo();
                List<NodeInfo> currentNodes = nodeService.getCurrentNodeInfo();
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

                res.setServiceInfo(serviceInfo);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    //Get the LogItem list of the specified traceId
    private List<LogItem> getLogItemListByCondition(String termName, String termValue){
        List<PodInfo> currentPods = podService.getCurrentPodInfo();
        List<NodeInfo> currentNodes = nodeService.getCurrentNodeInfo();

        List<LogItem> logItemList = new ArrayList<>();

        QueryBuilder qb = QueryBuilders.termQuery(termName,termValue);

        SearchResponse scrollResp = client.prepareSearch(LOGSTASH_LOG_INDEX).setTypes(LOGSTASH_LOG_TYPE)
                .addSort("time", SortOrder.ASC)
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

        //Default, it is not an error
        logItem.setIsError(0);

        Map<String, Object> map = hit.getSourceAsMap();
        //Set timestamp
        if(map.get("time") != null){
            String timestamp = map.get("time").toString();
            timestamp = timestamp.substring(0,23) + "Z";
            logItem.setTimestamp(MyUtil.getLocalTimeFromUTCFormat(timestamp));
        }

        try{
            LogBean logBean = mapper.readValue(hit.getSourceAsString(), LogBean.class);
//            logItem.setLogInfo(logBean.getLog());
            String logType = logBean.getLogType();
            logItem.setLogType(logType);

            logItem.setUri(logBean.getUri());

            setLogInfo(logItem, logType, map);

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

    //Set the log information according to the log type
    private void setLogInfo(BasicLogItem log, String logType, Map<String, Object> map) {
        if(logType.equals("InvocationRequest")){
            String requestPara = "No parameter!";
            if(map.get("Request") != null)
                requestPara = map.get("Request").toString();
            log.setLogInfo(String.format("[Request Parameter: %s]", requestPara));
        }else if(logType.equals("InvocationResponse")){
            String response = "No response!";
            String responseCode = "";
            String codeMessage = "";
            if(map.get("Response") != null)
                response = map.get("Response").toString();
            if(map.get("ResponseCode") != null)
                responseCode = map.get("ResponseCode").toString();
            if(map.get("CodeMessage") != null)
                codeMessage = map.get("CodeMessage").toString();
            log.setLogInfo(String.format("[Response Code: %s:%s][Response Value: %s]", responseCode, codeMessage, response));
        }else if(logType.equals("InternalMethod")){
            //InternalMethod: Normal and Message
            if(map.get("Content") != null)
                log.setLogInfo(map.get("Content").toString());
            else{
                String logInfo = String.format("[ExceptionMessage:%s][ExceptionCause:%s][ExceptionStack:%s]",
                        map.get("ExceptionMessage") != null?map.get("ExceptionMessage").toString():"",
                        map.get("ExceptionCause") != null?map.get("ExceptionCause").toString():"",
                        map.get("ExceptionStack") != null?map.get("ExceptionStack").toString():"");
                log.setLogInfo(logInfo);
                log.setIsError(2);
                if(logInfo.contains("error") || logInfo.contains("Error")){
                    log.setIsError(1);
                }
            }
        }else{
            log.setLogType("SystemLog");
            log.setLogInfo(map.get("log") != null ? map.get("log").toString() : "");
        }

//        if(Math.random() < 0.33){
//            log.setIsError(0);
//        }else if(Math.random() < 0.66){
//            log.setIsError(1);
//        }else{
//            log.setIsError(2);
//        }

    }

}
