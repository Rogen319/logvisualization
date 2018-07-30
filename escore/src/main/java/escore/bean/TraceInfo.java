package escore.bean;

import java.util.Set;

public class TraceInfo {
    private String traceId;
    private Set<String> serviceList;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;

    public TraceInfo(){

    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Set<String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(Set<String> serviceList) {
        this.serviceList = serviceList;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(int normalCount) {
        this.normalCount = normalCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }
}
