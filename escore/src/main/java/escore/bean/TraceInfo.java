package escore.bean;

import java.util.Set;

public class TraceInfo {
    private String traceId;
    private Set<String> serviceList;

    public TraceInfo(){

    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Set<String> getServiceName() {
        return serviceList;
    }

    public void setServiceName(Set<String> serviceList) {
        this.serviceList = serviceList;
    }
}
