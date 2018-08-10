package escore.response;

import escore.bean.SequenceInfo;

import java.io.Serializable;
import java.util.List;

public class TraceSequenceRes implements Serializable {

    private static final long serialVersionUID = 706588509693895946L;

    private String requestType;
    private int sequenceCount;
    private List<SequenceInfo> sequenceInfos;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public int getSequenceCount() {
        return sequenceCount;
    }

    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }

    public List<SequenceInfo> getSequenceInfos() {
        return sequenceInfos;
    }

    public void setSequenceInfos(List<SequenceInfo> sequenceInfos) {
        this.sequenceInfos = sequenceInfos;
    }
}
