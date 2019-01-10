package logapi.response;

import logapi.bean.InstanceAndTraceIdLog;

import java.util.List;

public class GetLogByServiceNameAndTraceIdRes extends GeneralResponse {
    private List<InstanceAndTraceIdLog> instanceWithLogList;

    public GetLogByServiceNameAndTraceIdRes() {

    }

    public List<InstanceAndTraceIdLog> getInstanceWithLogList() {
        return instanceWithLogList;
    }

    public void setInstanceWithLogList(List<InstanceAndTraceIdLog> instanceWithLogList) {
        this.instanceWithLogList = instanceWithLogList;
    }
}
