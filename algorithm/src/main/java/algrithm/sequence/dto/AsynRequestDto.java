package algrithm.sequence.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class AsynRequestDto implements Serializable {

    private static final long serialVersionUID = 3582453478771654745L;

    private String requestType;
    private long endTime;
    private long lookback;
    private Set<String> services;

    public AsynRequestDto() {
    }

    public AsynRequestDto(String requestType, long endTime, long lookback, Set<String> services) {
        this.requestType = requestType;
        this.endTime = endTime;
        this.lookback = lookback;
        this.services = services;
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
