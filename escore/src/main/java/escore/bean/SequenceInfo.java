package escore.bean;

import java.io.Serializable;
import java.util.List;

public class SequenceInfo implements Serializable {

    private static final long serialVersionUID = 8407511809837782187L;

    private String traceId;
    private List<String> serviceSequence;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public List<String> getServiceSequence() {
        return serviceSequence;
    }

    public void setServiceSequence(List<String> serviceSequence) {
        this.serviceSequence = serviceSequence;
    }
}
