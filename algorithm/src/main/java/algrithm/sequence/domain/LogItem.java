package algrithm.sequence.domain;

public class LogItem extends BasicLogItem{
    private TraceInfo traceInfo;
    private ServiceInfo serviceInfo;

    public LogItem(){

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
