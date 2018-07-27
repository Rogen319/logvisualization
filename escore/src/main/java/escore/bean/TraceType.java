package escore.bean;

import java.util.List;

public class TraceType {
    private String typeName;
    private List<TraceInfo> traceInfoList;
    private int count;

    public TraceType(){

    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public List<TraceInfo> getTraceInfoList() {
        return traceInfoList;
    }

    public void setTraceInfoList(List<TraceInfo> traceInfoList) {
        this.traceInfoList = traceInfoList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
