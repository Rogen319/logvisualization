package escore.bean;

public class ServiceInstanceWithTraceStatusCount {
    private int instanceNum;
    private int normalTraceCount;
    private int errorTraceCount;

    public ServiceInstanceWithTraceStatusCount() {

    }

    public int getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(int instanceNum) {
        this.instanceNum = instanceNum;
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
