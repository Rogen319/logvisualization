package algrithm.sequence.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class SequenceInfo implements Serializable {

    private static final long serialVersionUID = 8263889821493948169L;

    private List<String> serviceSequence;
    private Set<String> traceSet;
    private double errorRate;

    public SequenceInfo() {
    }

    public SequenceInfo(List<String> serviceSequence, Set<String> traceSet,
                        double errorRate) {
        this.serviceSequence = serviceSequence;
        this.traceSet = traceSet;
        this.errorRate = errorRate;
    }

    public List<String> getServiceSequence() {
        return serviceSequence;
    }

    public void setServiceSequence(List<String> serviceSequence) {
        this.serviceSequence = serviceSequence;
    }

    public Set<String> getTraceSet() {
        return traceSet;
    }

    public void setTraceSet(Set<String> traceSet) {
        this.traceSet = traceSet;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }
}
