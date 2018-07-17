package logapi.service;

import logapi.bean.InstanceInfo;
import logapi.bean.NodeInfo;
import logapi.bean.PodContainer;
import logapi.bean.PodInfo;
import logapi.response.GetPodsListResponse;
import logapi.response.QueryPodInfoRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PodService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestTemplate restTemplate;

    //Get current pod information
    public List<PodInfo> getCurrentPodInfo(){
        //Get and add pods information
        GetPodsListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getPodsList",
                GetPodsListResponse.class);
        if(result.isStatus()){
            return result.getPods();
        }
        return null;
    }

    //Set the instance information
    public NodeInfo setInstanceInfo(InstanceInfo instanceInfo, String podName, List<PodInfo> currentPods){
        String containerName = instanceInfo.getContainer().getName();
        NodeInfo nodeInfo = new NodeInfo();

        //Check if pod exist in the current pods
        for(PodInfo podInfo : currentPods){
            if(podInfo.getName().equals(podName)){
                nodeInfo.setName(podInfo.getNodeName());
                nodeInfo.setIp(podInfo.getNodeIP());
                log.info(String.format("Find [%s] in current pod list", podName));
                instanceInfo.setStatus(podInfo.getStatus());
                List<PodContainer> containers = podInfo.getContainers();
                if(containers != null){
                    for(PodContainer container : containers){
                        if(container.getName().equals(containerName)){
                            instanceInfo.getContainer().setImageName(container.getImageName());
                            instanceInfo.getContainer().setImageVersion(container.getImageVersion());
                            instanceInfo.getContainer().setPorts(container.getPorts());
                            return nodeInfo;
                        }
                    }
                }
            }
        }

        //Query pod information in the elasticsearch
        QueryPodInfoRes result = restTemplate.getForObject(
                "http://logvisualization-escore:17319/queryPodInfo/" + podName,
                QueryPodInfoRes.class);
        if(result.isStatus()){
            PodInfo podInfo = result.getPodInfo();
            nodeInfo.setName(podInfo.getNodeName());
            nodeInfo.setIp(podInfo.getNodeIP());
            instanceInfo.setStatus("Dead");
            List<PodContainer> containers = podInfo.getContainers();
            if(containers != null){
                for(PodContainer container : containers){
                    if(container.getName().equals(containerName)){
                        instanceInfo.getContainer().setImageName(container.getImageName());
                        instanceInfo.getContainer().setImageVersion(container.getImageVersion());
                        instanceInfo.getContainer().setPorts(container.getPorts());
                        return nodeInfo;
                    }
                }
            }
        }
        return null;
    }
}
