package k8sapi.service;

import com.alibaba.fastjson.JSON;
import k8sapi.bean.V1NodeList;
import k8sapi.config.ClusterConfig;
import k8sapi.util.MyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class NodeService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ClusterConfig clusterConfig;

    //Get the node list
    public V1NodeList getNodeList(){
        //Get the current nodes information and echo to the file
        String filePath = "/app/get_node_list_result_" + UUID.randomUUID().toString() + ".json";
        V1NodeList nodeList = new V1NodeList();
        String apiUrl = String.format("%s/api/v1/nodes",clusterConfig.getApiServer());
        log.info(String.format("The constructed api url for getting the node list is %s", apiUrl));
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
            //Parse the response to the V1NodeList Bean
            nodeList = JSON.parseObject(json,V1NodeList.class);
        } catch (IOException e) {
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return nodeList;
    }
}
