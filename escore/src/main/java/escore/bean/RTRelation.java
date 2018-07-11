package escore.bean;

public class RTRelation {
    private String requestType;
    private String traceId;

    public RTRelation(){

    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof RTRelation){
            RTRelation other = (RTRelation)obj;
            if(this.requestType.equals(other.getRequestType()) && this.traceId.equals(other.getTraceId()))
                return true;
            else
                return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        String id = this.requestType + this.traceId;
        return id.hashCode();
    }
}
