package algrithm.sequence.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SequenceTypeDetails {
    private List<String> sequence;
    private long failedTime;
    private long successTime;
    private Set<String> traceSet = new HashSet<>();

    public SequenceTypeDetails() {
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
        return traceSet;
    }

    public void setTraceSet(Set<String> traceSet) {
        this.traceSet = traceSet;
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
