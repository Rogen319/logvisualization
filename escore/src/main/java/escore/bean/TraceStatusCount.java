package escore.bean;

public class TraceStatusCount {
    private int normalTraceCount;
    private int errorTraceCount;

    public TraceStatusCount() {
        this.normalTraceCount = 0;
        this.errorTraceCount = 0;
    }

    public int getNormalTraceCount() {
        return normalTraceCount;
    }

    public void setNormalTraceCount(int normalTraceCount) {
        this.normalTraceCount = normalTraceCount;
    }

    public int getErrorTraceCount() {
        return errorTraceCount;
    }

    public void setErrorTraceCount(int errorTraceCount) {
        this.errorTraceCount = errorTraceCount;
    }
}
