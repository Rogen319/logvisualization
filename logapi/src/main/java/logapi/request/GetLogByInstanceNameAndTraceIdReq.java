package logapi.request;

public class GetLogByInstanceNameAndTraceIdReq {
    private String instanceName;
    private String traceId;

    public GetLogByInstanceNameAndTraceIdReq(){

    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
