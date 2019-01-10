package escore.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KubernetesMeatadata {
    private String namespace;
    private KubernetesMeatadataContainer container;
    private KubernetesMeatadataPod pod;

    public KubernetesMeatadata() {

    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public KubernetesMeatadataContainer getContainer() {
        return container;
    }

    public void setContainer(KubernetesMeatadataContainer container) {
        this.container = container;
    }

    public KubernetesMeatadataPod getPod() {
        return pod;
    }

    public void setPod(KubernetesMeatadataPod pod) {
        this.pod = pod;
    }
}
