package logapi.response;

import logapi.bean.BasicLogItem;
import logapi.bean.ServiceInfo;

import java.util.List;

public class GetLogByInstanceNameAndTraceIdRes extends GeneralResponse {
    private List<BasicLogItem> logs;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;
    private ServiceInfo serviceInfo;

    public GetLogByInstanceNameAndTraceIdRes(){

    }

    public List<BasicLogItem> getLogs() {
        return logs;
    }

    public void setLogs(List<BasicLogItem> logs) {
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

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}
