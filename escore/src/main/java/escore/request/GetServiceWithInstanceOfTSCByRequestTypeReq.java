package escore.request;

public class GetServiceWithInstanceOfTSCByRequestTypeReq {
    private String requestType;
    private long endTime;
    private long lookback;

    public GetServiceWithInstanceOfTSCByRequestTypeReq() {

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
