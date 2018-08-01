package escore.service;

import escore.bean.PodInfo;
import escore.response.GetPodsListResponse;
import escore.response.QueryPodInfoRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PodService {

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
    public String getServiceName(String podName, List<PodInfo> currentPods){
        //Check if pod exist in the current pods
        for(PodInfo podInfo : currentPods){
            if(podInfo.getName().equals(podName))
                return podInfo.getServiceName();
        }

        //Query pod information in the elasticsearch
        QueryPodInfoRes result = restTemplate.getForObject(
                "http://logvisualization-escore:17319/queryPodInfo/" + podName,
                QueryPodInfoRes.class);
        if(result.isStatus()){
            PodInfo podInfo = result.getPodInfo();
            return podInfo.getServiceName();
        }
        return "";
    }
}
