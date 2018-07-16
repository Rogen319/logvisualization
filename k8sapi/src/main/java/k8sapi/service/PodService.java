package k8sapi.service;

import com.alibaba.fastjson.JSON;
import k8sapi.bean.V1PodList;
import k8sapi.config.ClusterConfig;
import k8sapi.util.MyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PodService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ClusterConfig clusterConfig;

    //Get the pods list
    public V1PodList getPodList(){
        //Get the current pods information and echo to the file
        String filePath = "/app/get_pod_list_result_" + System.currentTimeMillis()+ ".json";
        V1PodList podList = new V1PodList();
        String apiUrl = String.format("%s/api/v1/namespaces/%s/pods",clusterConfig.getApiServer(),MyUtil.DEFAULT_NAMESPACE);
        log.info(String.format("The constructed api url for getting the pod list is %s", apiUrl));
        String[] cmds ={
                "/bin/sh","-c",String.format("curl -X GET %s --header \"Authorization: Bearer %s\" --insecure >> %s",apiUrl,clusterConfig.getToken(),filePath)
        };
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
            p.waitFor();

            String json = MyUtil.readWholeFile(filePath);
            //Parse the response to the V1PodList Bean
            podList = JSON.parseObject(json,V1PodList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return podList;
    }
}
