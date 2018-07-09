package k8sapi.service;

import k8sapi.bean.*;
import k8sapi.response.GetNodesListResponse;
import k8sapi.response.GetPodsListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KubernetesAPIServiceImpl implements KubernetesAPIService {
    @Autowired
    NodeService nodeService;

    @Autowired
    PodService podService;

    @Override
    public GetNodesListResponse getNodesList() {
        GetNodesListResponse response = new GetNodesListResponse();
        V1NodeList nodeList = nodeService.getNodeList();
        System.out.println(String.format("There are now %d nodes in the cluster now", nodeList.getItems().size()));
        if(nodeList.getItems().size() < 1){
            response.setStatus(false);
            response.setMessage("There is no nodes in the cluster!");
            response.setNodes(null);
        }
        //Construct the nodeinfo list
        List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        for(V1Node node : nodeList.getItems()){
            NodeInfo nodeInfo = new NodeInfo();
            System.out.println(String.format("The node name is %s and the role is %s",node.getMetadata().getName(),node.getSpec().getTaints() == null?"Minion":"Master"));
            //Set the role
            if(node.getSpec().getTaints() != null)
                nodeInfo.setRole("Master");
            else
                nodeInfo.setRole("Minion");
            //Set the name
            nodeInfo.setName(node.getMetadata().getName());
            V1NodeStatus status = node.getStatus();
            //Set the ip
            List<V1NodeAddress> addresses = status.getAddresses();
            for(V1NodeAddress address : addresses){
                if(address.getType().equals("InternalIP")){
                    nodeInfo.setIp(address.getAddress());
                    break;
                }
            }
            //Set the status
            List<V1NodeCondition> conditions = status.getConditions();
            for(V1NodeCondition condition : conditions){
                if(condition.getType().equals("Ready")){
                    if(condition.getStatus().equals("True")){
                        nodeInfo.setStatus("Ready");
                    }
                    else{
                        nodeInfo.setStatus("NotReady");
                    }
                    break;
                }
            }
            //Set the node info
            V1NodeSystemInfo systemInfo = status.getNodeInfo();
            if(systemInfo != null){
                nodeInfo.setContainerRuntimeVersion(systemInfo.getContainerRuntimeVersion());
                nodeInfo.setKubeletVersion(systemInfo.getKubeletVersion());
                nodeInfo.setKubeProxyVersion(systemInfo.getKubeProxyVersion());
                nodeInfo.setOperatingSystem(systemInfo.getOperatingSystem());
                nodeInfo.setOsImage(systemInfo.getOsImage());
            }
            nodeInfos.add(nodeInfo);
        }
        response.setStatus(true);
        response.setMessage("Succeed to get the node list info!");
        response.setNodes(nodeInfos);
        return response;
    }

    @Override
    public GetPodsListResponse getPodsListAPI() {
        GetPodsListResponse response = new GetPodsListResponse();
        V1PodList podList = podService.getPodList();
        System.out.println(String.format("There are now %d pods in the cluster now", podList.getItems().size()));
        if(podList.getItems().size() < 1){
            response.setStatus(true);
            response.setMessage("No resource found!");
            response.setPods(null);
        }
        //Construct the podinfo list
        List<PodInfo> podInfos = new ArrayList<PodInfo>();
        for(V1Pod pod : podList.getItems()){
            PodInfo podInfo = new PodInfo();
            podInfo.setName(pod.getMetadata().getName());
            podInfo.setStatus(pod.getStatus().getPhase());
            podInfo.setNodeName(pod.getSpec().getNodeName());
            podInfo.setNodeIP(pod.getStatus().getHostIP());
            podInfo.setPodIP(pod.getStatus().getPodIP());
            podInfo.setStartTime(pod.getStatus().getStartTime());
            podInfos.add(podInfo);
        }
        response.setStatus(true);
        response.setMessage("Successfully get the pod info list!");
        response.setPods(podInfos);
        return response;
    }
}