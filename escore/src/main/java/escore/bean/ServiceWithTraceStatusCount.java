package escore.bean;

public class ServiceWithTraceStatusCount {
    private String serviceName;
    private int normalTraceCount;
    private int errorTraceCount;

    public ServiceWithTraceStatusCount(){

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
