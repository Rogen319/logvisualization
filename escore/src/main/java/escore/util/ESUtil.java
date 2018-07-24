package escore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.bean.RTRelation;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

    //Get the stored relation information
    public List<RTRelation> getStoredRelations(){
        List<RTRelation> relations = new ArrayList<>();

        SearchResponse response = client.prepareSearch("rt_relation").setTypes("relation").setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        log.info(String.format("The length of relation search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                log.info(hit.getSourceAsString());
                RTRelation relation = mapper.readValue(hit.getSourceAsString(), RTRelation.class);
                relations.add(relation);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return relations;
    }

    //Convert the time from milliseconds to specified format
    public String convertTime(long milliseconds){
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter.format(date);
    }
}
