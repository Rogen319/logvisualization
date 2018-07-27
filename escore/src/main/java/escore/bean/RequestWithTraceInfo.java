package escore.bean;

import java.util.List;

public class RequestWithTraceInfo {
    private String requestType;
    private List<TraceType> traceTypeList;

    public RequestWithTraceInfo(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<TraceType> getTraceTypeList() {
        return traceTypeList;
    }

    public void setTraceTypeList(List<TraceType> traceTypeList) {
        this.traceTypeList = traceTypeList;
    }
}
