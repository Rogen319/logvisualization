package escore.bean;

import java.util.List;

public class RequestWithTraceInfo {
    private String requestType;
    private List<TraceType> traceTypeList;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;
    private int normalTraceCount;
    private int errorTraceCount;

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

    public int getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(int normalCount) {
        this.normalCount = normalCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public int getNormalTraceCount() {
        return normalTraceCount;
    }

    public void setNormalTraceCount(int normalTraceCount) {
        this.normalTraceCount = normalTraceCount;
    }

    public int getErrorTraceCount() {
        return errorTraceCount;
    }

    public void setErrorTraceCount(int errorTraceCount) {
        this.errorTraceCount = errorTraceCount;
    }
}
