package algrithm.sequence.dto;

import java.io.Serializable;
import java.util.List;

public class RequestTypeSequenceDto implements Serializable {

    private static final long serialVersionUID = 222159767701965927L;

    private String requestTypeName;
    private List<TraceTypeSequenceDto> traceTypes;

    public RequestTypeSequenceDto() {
    }

    public RequestTypeSequenceDto(String requestTypeName, List<TraceTypeSequenceDto> traceTypes) {
        this.requestTypeName = requestTypeName;
        this.traceTypes = traceTypes;
    }

    public String getRequestTypeName() {
        return requestTypeName;
    }

    public void setRequestTypeName(String requestTypeName) {
        this.requestTypeName = requestTypeName;
    }

    public List<TraceTypeSequenceDto> getTraceTypes() {
        return traceTypes;
    }

    public void setTraceTypes(List<TraceTypeSequenceDto> traceTypes) {
        this.traceTypes = traceTypes;
    }
}
