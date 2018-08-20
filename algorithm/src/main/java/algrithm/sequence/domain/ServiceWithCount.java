package algrithm.sequence.domain;

import java.io.Serializable;

public class ServiceWithCount implements Serializable{

    private static final long serialVersionUID = -2579305877299900014L;
    private String serviceName;
    private int normalCount;
    private int errorCount;
    private int exceptionCount;

    public ServiceWithCount() {

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(int normalCount) {
        this.normalCount = normalCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(int exceptionCount) {
        this.exceptionCount = exceptionCount;
    }
}
