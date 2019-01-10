package escore.request;

public class GetRequestWithTraceIDByTimeRangeReq {
    private long endTime;
    private long lookback;

    public GetRequestWithTraceIDByTimeRangeReq() {

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
