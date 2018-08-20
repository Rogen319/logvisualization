package escore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.bean.TraceStatus;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ESUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransportClient client;

    //Get the stored pods information
    public List<PodInfo> getStoredPods(){
        List<PodInfo> pods = new ArrayList<>();

        SearchResponse response = client.prepareSearch("k8s_*").setTypes("pod").setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        log.info(String.format("The length of pod search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                log.info(hit.getSourceAsString());
                PodInfo pod = mapper.readValue(hit.getSourceAsString(), PodInfo.class);
                pods.add(pod);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return pods;
    }

    //Get the stored nodes information
    public List<NodeInfo> getStoredNodes(){
        List<NodeInfo> nodes = new ArrayList<>();

        SearchResponse response = client.prepareSearch("k8s_*").setTypes("node").setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        log.info(String.format("The length of node search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                log.info(hit.getSourceAsString());
                NodeInfo node = mapper.readValue(hit.getSourceAsString(), NodeInfo.class);
                nodes.add(node);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return nodes;
    }

    //Get the stored trace status information
    public List<TraceStatus> getExistedTraceStatus(){
        List<TraceStatus> res = new ArrayList<>();

        QueryBuilder qb = QueryBuilders.matchAllQuery();

        SearchResponse scrollResp = client.prepareSearch(Const.TRACE_STATUS_INDEX).setTypes(Const.TRACE_STATUS_TYPE)
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
                if (map.get("traceId") != null && map.get("status") != null) {
                    TraceStatus traceStatus = new TraceStatus();
                    traceStatus.setTraceId(map.get("traceId").toString());
                    traceStatus.setStatus(map.get("status").toString());
                }
            }
        }

        return res;
    }

    //Get the status of specified trace
    public String getStatusOfTrace(String traceId){
        //First, check if the trace exist in the trace_status index
        QueryBuilder qb = QueryBuilders.termQuery("traceId", traceId);
        SearchResponse response = client.prepareSearch(Const.TRACE_STATUS_INDEX)
                .setTypes(Const.TRACE_STATUS_TYPE)
                .setQuery(qb)
                .get();

        SearchHit[] hits = response.getHits().getHits();
        Map<String, Object> map;

        if(hits.length > 0){
            map = hits[0].getSourceAsMap();
            if(map.get("status") != null)
                return map.get("status").toString();
        }

        //If not exists, then check the log to determine the status
        qb = QueryBuilders.termQuery("TraceId", traceId);

        SearchResponse scrollResp = client.prepareSearch(Const.LOGSTASH_LOG_INDEX).setTypes(Const.LOGSTASH_LOG_TYPE)
                .setScroll(new TimeValue(60000))
                .setQuery(qb)
                .setSize(100).get(); //max of 100 hits will be returned for each scroll

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

    //Convert the time from milliseconds to specified format
    public String convertTime(long milliseconds){
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter.format(date);
    }
}
