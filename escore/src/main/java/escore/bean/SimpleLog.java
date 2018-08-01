package escore.bean;

public class SimpleLog {
    private String serviceName;
    private int isError;

    public SimpleLog(){

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getIsError() {
        return isError;
    }

    public void setIsError(int isError) {
        this.isError = isError;
    }
}
