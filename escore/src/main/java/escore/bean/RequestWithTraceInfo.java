package escore.bean;

import java.util.List;

public class RequestWithTraceInfo {
    private String requestType;
    private List<TraceInfo> traceInfoList;
    private int count;

    public RequestWithTraceInfo(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<TraceInfo> getTraceInfoList() {
        return traceInfoList;
    }

    public void setTraceInfoList(List<TraceInfo> traceInfoList) {
        this.traceInfoList = traceInfoList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
