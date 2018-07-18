package k8sapi.bean;

public class V1ClientIPConfig {
    private Integer timeoutSeconds = null;

    public V1ClientIPConfig(){

    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
