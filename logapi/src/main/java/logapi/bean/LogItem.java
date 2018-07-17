package logapi.bean;

public class LogItem {
    private String timestamp;
    private String logType;
    private String requestType;
    private String logInfo;
    private TraceInfo traceInfo;
    private ServiceInfo serviceInfo;

    public LogItem(){

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public void setTraceInfo(TraceInfo traceInfo) {
        this.traceInfo = traceInfo;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
