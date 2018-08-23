package algrithm.sequence.dto;

import algrithm.sequence.domain.SequenceInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class TraceTypeSequenceDto extends GeneralResponse implements Serializable {

    private static final long serialVersionUID = -2512631240886515425L;

    private boolean asyn;
    private Set<String> asynService;
    private List<SequenceInfo> sequences;

    public TraceTypeSequenceDto() {
    }

    public TraceTypeSequenceDto(boolean asyn, Set<String> asynService,
                                List<SequenceInfo> sequences) {
        this.asyn = asyn;
        this.asynService = asynService;
        this.sequences = sequences;
    }

    public boolean isAsyn() {
        return asyn;
    }

    public void setAsyn(boolean asyn) {
        this.asyn = asyn;
    }

    public Set<String> getAsynService() {
        return asynService;
    }

    public void setAsynService(Set<String> asynService) {
        this.asynService = asynService;
    }

    public List<SequenceInfo> getSequences() {
        return sequences;
    }

    public void setSequences(List<SequenceInfo> sequences) {
        this.sequences = sequences;
    }
}
