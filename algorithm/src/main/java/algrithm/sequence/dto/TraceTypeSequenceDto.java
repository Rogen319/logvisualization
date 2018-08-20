package algrithm.sequence.dto;

import java.io.Serializable;
import java.util.List;

public class TraceTypeSequenceDto implements Serializable{

    private static final long serialVersionUID = -2512631240886515425L;

    private String traceType;
    private boolean asyn;
    private List<String> asynService;

    public TraceTypeSequenceDto() {
    }

    public TraceTypeSequenceDto(String traceType, boolean asyn, List<String> asynService) {
        this.traceType = traceType;
        this.asyn = asyn;
        this.asynService = asynService;
    }

    public String getTraceType() {
        return traceType;
    }

    public void setTraceType(String traceType) {
        this.traceType = traceType;
    }

    public boolean isAsyn() {
        return asyn;
    }

    public void setAsyn(boolean asyn) {
        this.asyn = asyn;
    }

    public List<String> getAsynService() {
        return asynService;
    }

    public void setAsynService(List<String> asynService) {
        this.asynService = asynService;
    }
}
