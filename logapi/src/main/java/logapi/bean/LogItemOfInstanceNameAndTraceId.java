package logapi.bean;

public class LogItemOfInstanceNameAndTraceId {
    private String timestamp;
    private String logType;
    private String requestType;
    private String logInfo;

    public LogItemOfInstanceNameAndTraceId(){

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
}
