package logapi.response;

import logapi.bean.LogItem;

import java.util.List;

public class LogResponse extends GeneralResponse {
    private List<LogItem> logs;

    public LogResponse(){

    }

    public List<LogItem> getLogs() {
        return logs;
    }

    public void setLogs(List<LogItem> logs) {
        this.logs = logs;
    }
}
