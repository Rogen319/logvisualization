package escore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import escore.bean.NodeInfo;
import escore.bean.PodInfo;
import escore.init.InitIndexAndType;
import escore.response.GetNodesListResponse;
import escore.response.GetPodsListResponse;
import escore.util.ESUtil;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ESCoreScheduledTasks {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TransportClient client;

    @Autowired
    private ESUtil esUtil;

    //Update pod info every 30 minutes  30000  1800000
    @Scheduled(initialDelay = 9000, fixedDelay = 60000)
    public void updatePodInfo(){
        System.out.println("===Update the pod info===");
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
                        System.out.println(String.format("Begin to add the pod [%s] with ip address [%s]", pod.getName(), pod.getPodIP()));
                        byte[] json = mapper.writeValueAsBytes(pod);
                        client.prepareIndex(InitIndexAndType.K8S_INDEX_POD,"pod").setSource(json, XContentType.JSON).get();
                        System.out.println(String.format("Add the pod [%s] with ip address [%s]", pod.getName(), pod.getPodIP()));
                    }else{
                        System.out.println(String.format("The pod [%s] with ip address [%s] already exists!", pod.getName(), pod.getPodIP()));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //Update node info every 1 hour 30000 3600000
    @Scheduled(initialDelay = 27000, fixedDelay = 60000)
    public void updateNodeInfo(){
        System.out.println("===Update the node information===");
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
                        System.out.println(String.format("Begin to add the node [%s] with ip address [%s]", node.getName(), node.getIp()));
                        byte[] json = mapper.writeValueAsBytes(node);
                        client.prepareIndex(InitIndexAndType.K8S_INDEX_NODE,"node").setSource(json, XContentType.JSON).get();
                        System.out.println(String.format("Add the node [%s] with ip address [%s]", node.getName(), node.getIp()));
                    }else{
                        System.out.println(String.format("The node [%s] with ip address [%s] already exists!", node.getName(), node.getIp()));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

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
