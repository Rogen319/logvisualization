package algrithm.sequence.domain;

public class ServiceNameWithLogType {
    private String serviceName;
    private String LogType;

    public ServiceNameWithLogType() {
    }

    public ServiceNameWithLogType(String serviceName, String logType) {
        this.serviceName = serviceName;
        LogType = logType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getLogType() {
        return LogType;
    }

    public void setLogType(String logType) {
        LogType = logType;
    }
}
