package algrithm.sequence.dto;



import algrithm.sequence.domain.LogItem;

import java.util.List;

public class LogDto extends GeneralResponse {
    private List<LogItem> logs;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;

    public LogDto() {

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
