package k8sapi.bean;

import java.util.ArrayList;
import java.util.List;

public class V1ServiceList {
    private String apiVersion;
    private List<V1Service> items = new ArrayList<V1Service>();
    private String kind;
    private V1ListMeta metadata;

    public V1ServiceList(){

    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public List<V1Service> getItems() {
        return items;
    }

    public void setItems(List<V1Service> items) {
        this.items = items;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public V1ListMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(V1ListMeta metadata) {
        this.metadata = metadata;
    }
}
