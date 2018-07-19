package logapi.response;

import logapi.bean.LogItemOfInstanceNameAndTraceId;

import java.util.List;

public class GetLogByInstanceNameAndTraceIdRes extends GeneralResponse {
    private List<LogItemOfInstanceNameAndTraceId> logs;

    public GetLogByInstanceNameAndTraceIdRes(){

    }

    public List<LogItemOfInstanceNameAndTraceId> getLogs() {
        return logs;
    }

    public void setLogs(List<LogItemOfInstanceNameAndTraceId> logs) {
        this.logs = logs;
    }
}
