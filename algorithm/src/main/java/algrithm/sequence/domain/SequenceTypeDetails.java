package algrithm.sequence.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceTypeDetails {
    private List<String> sequence;
    private long failedTime;
    private long successTime;
    private Set<String> TraceSet = new HashSet<>();

    public SequenceTypeDetails() {
    }

    public SequenceTypeDetails(List<String> sequence, long failedTime, long successTime, Set<String> traceSet) {
        this.sequence = sequence;
        this.failedTime = failedTime;
        this.successTime = successTime;
        TraceSet = traceSet;
    }

    public List<String> getSequence() {
        return sequence;
    }

    public void setSequence(List<String> sequence) {
        this.sequence = sequence;
    }

    public long getFailedTime() {
        return failedTime;
    }

    public void setFailedTime(long failedTime) {
        this.failedTime = failedTime;
    }

    public long getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(long successTime) {
        this.successTime = successTime;
    }

    public Set<String> getTraceSet() {
        return TraceSet;
    }

    public void setTraceSet(Set<String> traceSet) {
        TraceSet = traceSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceTypeDetails that = (SequenceTypeDetails) o;

        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
}
