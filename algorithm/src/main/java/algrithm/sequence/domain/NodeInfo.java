package algrithm.sequence.domain;

public class NodeInfo {
    private String role;
    private String name;
    private String ip;
    private String status;
    private String kubeProxyVersion = null;
    private String kubeletVersion = null;
    private String operatingSystem = null;
    private String osImage = null;
    private String containerRuntimeVersion = null;

    public NodeInfo(){

    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKubeProxyVersion() {
        return kubeProxyVersion;
    }

    public void setKubeProxyVersion(String kubeProxyVersion) {
        this.kubeProxyVersion = kubeProxyVersion;
    }

    public String getKubeletVersion() {
        return kubeletVersion;
    }

    public void setKubeletVersion(String kubeletVersion) {
        this.kubeletVersion = kubeletVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOsImage() {
        return osImage;
    }

    public void setOsImage(String osImage) {
        this.osImage = osImage;
    }

    public String getContainerRuntimeVersion() {
        return containerRuntimeVersion;
    }

    public void setContainerRuntimeVersion(String containerRuntimeVersion) {
        this.containerRuntimeVersion = containerRuntimeVersion;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof NodeInfo){
            NodeInfo other = (NodeInfo)obj;
            if(this.getName().equals(other.getName()) && this.getIp().equals(other.getIp()))
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String id = this.name + this.ip;
        return id.hashCode();
    }
}
