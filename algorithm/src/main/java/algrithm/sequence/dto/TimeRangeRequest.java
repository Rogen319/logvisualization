package algrithm.sequence.dto;

import java.io.Serializable;

public class TimeRangeRequest implements Serializable{

    private static final long serialVersionUID = -3787962260303567126L;

    private long endTime;
    private long lookback;

    public TimeRangeRequest() {
    }

    public TimeRangeRequest(long endTime, long lookback) {
        this.endTime = endTime;
        this.lookback = lookback;
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
