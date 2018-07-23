package logapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import logapi.bean.*;
import logapi.request.GetLogByInstanceNameAndTraceIdReq;
import logapi.response.GetLogByInstanceNameAndTraceIdRes;
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

    //Get log by trace id
    @Override
    public LogResponse getLogByTraceId(String traceId) {
        LogResponse res = new LogResponse();

        List<LogItem> logs = getLogItemListByCondition("TraceId", traceId);
        res.setLogs(logs);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the trace log. Size is [%d].", logs.size()));

        return res;
    }

    //Get log by instance name
    @Override
    public LogResponse getLogByInstanceName(String instanceName) {
        LogResponse res = new LogResponse();

        List<LogItem> logs = getLogItemListByCondition("kubernetes.pod.name.keyword",instanceName);
        res.setLogs(logs);
        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the instance log. Size is [%d].", logs.size()));

        return res;
    }

    @Override
    public GetLogByInstanceNameAndTraceIdRes getLogByInstanceNameAndTraceId(GetLogByInstanceNameAndTraceIdReq request) {
        GetLogByInstanceNameAndTraceIdRes res = new GetLogByInstanceNameAndTraceIdRes();

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("kubernetes.pod.name.keyword",request.getInstanceName()))
                .must(QueryBuilders.termQuery("TraceId",request.getTraceId()));

        List<BasicLogItem> logs = new ArrayList<>();
        SearchResponse scrollResp = client.prepareSearch(LOGSTASH_LOG_INDEX).setTypes(LOGSTASH_LOG_TYPE)
                .addSort("@timestamp", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll
        //Scroll until no hits are returned
        SearchHit[] hits;
        Map<String, Object> map;

        while(scrollResp.getHits().getHits().length != 0){ // Zero hits mark the end of the scroll and the while loop
            hits = scrollResp.getHits().getHits();
            for (SearchHit hit : hits) {
                //Handle the hit
                map = hit.getSourceAsMap();
                BasicLogItem log = new BasicLogItem();
                if(map.get("@timestamp") != null){
                    String timestamp = map.get("@timestamp").toString();
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
//                if(map.get("log") != null){
//                    log.setLogInfo(map.get("log").toString());
//                }
                logs.add(log);
            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        log.info(String.format("The length of corresponding logitem list is [%d]", logs.size()));

        res.setStatus(true);
        res.setMessage(String.format("Succeed to get the logs of instance:[%s] with traceid:[%s]. Size is [%d]",
                request.getInstanceName(),request.getTraceId(),logs.size()));
        res.setLogs(logs);

        return res;
    }

    //Get the LogItem list of the specified traceId
    private List<LogItem> getLogItemListByCondition(String termName, String termValue){
        List<PodInfo> currentPods = podService.getCurrentPodInfo();
        List<NodeInfo> currentNodes = nodeService.getCurrentNodeInfo();

        List<LogItem> logItemList = new ArrayList<>();

        QueryBuilder qb = QueryBuilders.termQuery(termName,termValue);

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
//            logItem.setLogInfo(logBean.getLog());
            String logType = logBean.getLogType();
            logItem.setLogType(logType);
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

    //Set the log information of log item
    //Set the log information according to the log type
    private void setLogInfo(BasicLogItem log, String logType, Map<String, Object> map) {
        if(logType.equals("InvocationRequest")){
            String request = "No parameter!";
            if(map.get("Request") != null)
                request = map.get("Request").toString();
            log.setLogInfo(String.format("[Request Parameter: %s]", request));
        }else if(logType.equals("InvocationResponse")){
            String response = "No response!";
            if(map.get("Response") != null)
                response = map.get("Response").toString();
            log.setLogInfo(String.format("[Response Value: %s]", response));
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
            }
        }else{
            log.setLogType("SystemLog");
            log.setLogInfo(map.get("log") != null ? map.get("log").toString() : "");
        }
    }
}
