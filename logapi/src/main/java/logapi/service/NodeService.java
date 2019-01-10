package logapi.service;

import logapi.bean.NodeInfo;
import logapi.response.GetNodesListResponse;
import logapi.response.QueryNodeInfoRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NodeService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestTemplate restTemplate;

    //Get current pod information
    public List<NodeInfo> getCurrentNodeInfo() {
        //Get and add pods information
        GetNodesListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getNodesList",
                GetNodesListResponse.class);
        if (result.isStatus()) {
            return result.getNodes();
        }
        return null;
    }

    //Set the instance information
    public void setNodeInfo(NodeInfo nodeInfo, List<NodeInfo> currentNodes) {

        //Check if pod exist in the current nodes
        for (NodeInfo node : currentNodes) {
            if (node.equals(nodeInfo)) {
                log.info("Find corresponding node in the current node list");
                nodeInfo.setContainerRuntimeVersion(node.getContainerRuntimeVersion());
                nodeInfo.setKubeletVersion(node.getKubeletVersion());
                nodeInfo.setKubeProxyVersion(node.getKubeProxyVersion());
                nodeInfo.setOperatingSystem(node.getOperatingSystem());
                nodeInfo.setOsImage(node.getOsImage());
                nodeInfo.setRole(node.getRole());
                nodeInfo.setStatus(node.getStatus());
                return;
            }
        }

        //Query node information in the elasticsearch
        QueryNodeInfoRes result = restTemplate.postForObject(
                "http://logvisualization-escore:17319/queryNodeInfo",
                nodeInfo,
                QueryNodeInfoRes.class);
        if (result.isStatus()) {
            log.info("Find corresponding node in the stored(es) node list");
            NodeInfo node = result.getNodeInfo();
            nodeInfo.setContainerRuntimeVersion(node.getContainerRuntimeVersion());
            nodeInfo.setKubeletVersion(node.getKubeletVersion());
            nodeInfo.setKubeProxyVersion(node.getKubeProxyVersion());
            nodeInfo.setOperatingSystem(node.getOperatingSystem());
            nodeInfo.setOsImage(node.getOsImage());
            nodeInfo.setRole(node.getRole());
            nodeInfo.setStatus("Dead");
        }
    }
}
