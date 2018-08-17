package escore.request;

import java.util.List;
import java.util.Set;

public class GetServiceWithTraceCountByTraceTypeReq {
    private String requestType;
    private long endTime;
    private long lookback;
    private Set<String> services;

    public GetServiceWithTraceCountByTraceTypeReq(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getLookback() {
        return lookback;
    }

    public void setLookback(long lookback) {
        this.lookback = lookback;
    }

    public Set<String> getServices() {
        return services;
    }

    public void setServices(Set<String> services) {
        this.services = services;
    }
}
