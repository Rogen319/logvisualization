package logapi.bean;

import java.util.List;
import java.util.Map;

public class PodInfo {
    private String name;
    private String nodeName;
    private String serviceName;
    private String status;
    private String nodeIP;
    private String podIP;
    private String startTime;
    private Map<String, String> labels;
    private List<PodContainer> containers;

    public PodInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public String getPodIP() {
        return podIP;
    }

    public void setPodIP(String podIP) {
        this.podIP = podIP;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public List<PodContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<PodContainer> containers) {
        this.containers = containers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof PodInfo) {
            PodInfo other = (PodInfo) obj;
            if (this.getName().equals(other.getName()) && this.getPodIP().equals(other.getPodIP()))
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String id = this.name + this.podIP;
        return id.hashCode();
    }
}
