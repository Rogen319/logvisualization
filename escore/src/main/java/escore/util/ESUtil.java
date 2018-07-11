package escore.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.bean.RTRelation;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ESUtil {
    @Autowired
    private TransportClient client;

    //Get the stored pods information
    public List<PodInfo> getStoredPods(){
        List<PodInfo> pods = new ArrayList<>();

        SearchResponse response = client.prepareSearch("k8s_*").setTypes("pod").setSize(10000).get();

        SearchHit[] hits = response.getHits().getHits();
        System.out.println(String.format("The length of pod search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                System.out.println(hit.getSourceAsString());
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
        System.out.println(String.format("The length of node search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                System.out.println(hit.getSourceAsString());
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
        System.out.println(String.format("The length of relation search hits is [%d]", hits.length));

        ObjectMapper mapper = new ObjectMapper();
        try{
            for (SearchHit hit : hits) {
//                System.out.println(hit.getSourceAsString());
                RTRelation relation = mapper.readValue(hit.getSourceAsString(), RTRelation.class);
                relations.add(relation);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return relations;
    }
}
