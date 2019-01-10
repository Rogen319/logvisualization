package logapi.request;

public class GetLogByInstanceNameAndTraceIdReq {
    private String serviceName;
    private String traceId;

    public GetLogByInstanceNameAndTraceIdReq() {

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
