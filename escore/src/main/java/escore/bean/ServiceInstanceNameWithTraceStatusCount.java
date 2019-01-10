package escore.bean;

public class ServiceInstanceNameWithTraceStatusCount {
    private String instanceName;
    private int normalTraceCount;
    private int errorTraceCount;

    public ServiceInstanceNameWithTraceStatusCount() {

    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getNormalTraceCount() {
        return normalTraceCount;
    }

    public void setNormalTraceCount(int normalTraceCount) {
        this.normalTraceCount = normalTraceCount;
    }

    public int getErrorTraceCount() {
        return errorTraceCount;
    }

    public void setErrorTraceCount(int errorTraceCount) {
        this.errorTraceCount = errorTraceCount;
    }
}
