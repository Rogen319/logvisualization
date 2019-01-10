package k8sapi.bean;

public class V1Service {
    private String apiVersion = null;
    private String kind = null;
    private V1ObjectMeta metadata = null;
    private V1ServiceSpec spec = null;
    private V1ServiceStatus status = null;

    public V1Service() {

    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public V1ObjectMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(V1ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public V1ServiceSpec getSpec() {
        return spec;
    }

    public void setSpec(V1ServiceSpec spec) {
        this.spec = spec;
    }

    public V1ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(V1ServiceStatus status) {
        this.status = status;
    }
}
