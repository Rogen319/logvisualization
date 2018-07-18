package k8sapi.bean;

public class V1SessionAffinityConfig {
    private V1ClientIPConfig clientIP = null;

    public V1SessionAffinityConfig(){

    }

    public V1ClientIPConfig getClientIP() {
        return clientIP;
    }

    public void setClientIP(V1ClientIPConfig clientIP) {
        this.clientIP = clientIP;
    }
}
