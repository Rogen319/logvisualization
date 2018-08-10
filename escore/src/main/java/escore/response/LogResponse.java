package escore.response;


import escore.bean.LogItem;

import java.util.List;

public class LogResponse extends GeneralResponse {
    private List<LogItem> logs;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;

    public LogResponse(){

    }

    public List<LogItem> getLogs() {
        return logs;
    }

    public void setLogs(List<LogItem> logs) {
        this.logs = logs;
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
}
