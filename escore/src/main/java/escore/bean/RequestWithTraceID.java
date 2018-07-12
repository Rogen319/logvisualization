package escore.bean;

import java.util.List;

public class RequestWithTraceID {
    private String requestType;
    private List<String> traceIDs;
    private int count;

    public RequestWithTraceID(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<String> getTraceIDs() {
        return traceIDs;
    }

    public void setTraceIDs(List<String> traceIDs) {
        this.traceIDs = traceIDs;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
