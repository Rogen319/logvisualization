package k8sapi.bean;

public class V1LoadBalancerIngress {
    private String hostname;
    private String ip;

    public V1LoadBalancerIngress() {

    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
