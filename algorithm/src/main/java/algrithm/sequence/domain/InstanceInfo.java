package algrithm.sequence.domain;

public class InstanceInfo {
    private String instanceName;
    private String status;
    private PodContainer container;

    public InstanceInfo(){

    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PodContainer getContainer() {
        return container;
    }

    public void setContainer(PodContainer container) {
        this.container = container;
    }
}
