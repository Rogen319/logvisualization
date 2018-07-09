package escore.bean;

import java.util.List;

public class PodInfo {
    private String name;
    private String nodeName;
    private String status;
    private String nodeIP;
    private String podIP;
    private String startTime;
    private List<PodContainer> containers;

    public PodInfo(){

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

    public List<PodContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<PodContainer> containers) {
        this.containers = containers;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof PodInfo){
            PodInfo other = (PodInfo)obj;
            if(this.getName().equals(other.getName()) && this.getPodIP().equals(other.getPodIP()))
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
