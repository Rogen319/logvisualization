package algrithm.sequence.dto;

import java.io.Serializable;

public class TraceIdsRequestDto implements Serializable {

    private static final long serialVersionUID = -6109999757383692252L;

    private String requestType;
    private long endTime;
    private long lookback;


    public TraceIdsRequestDto() {
    }

    public TraceIdsRequestDto(String requestType, long endTime, long lookback) {
        this.requestType = requestType;
        this.endTime = endTime;
        this.lookback = lookback;
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
}
