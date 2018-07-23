package logapi.response;

import logapi.bean.BasicLogItem;

import java.util.List;

public class GetLogByInstanceNameAndTraceIdRes extends GeneralResponse {
    private List<BasicLogItem> logs;

    public GetLogByInstanceNameAndTraceIdRes(){

    }

    public List<BasicLogItem> getLogs() {
        return logs;
    }

    public void setLogs(List<BasicLogItem> logs) {
        this.logs = logs;
    }
}
