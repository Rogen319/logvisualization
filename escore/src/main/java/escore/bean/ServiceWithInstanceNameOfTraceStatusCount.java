package escore.bean;

import java.util.List;

public class ServiceWithInstanceNameOfTraceStatusCount {
    private String serviceName;
    private List<ServiceInstanceNameWithTraceStatusCount> sinwtscList;

    public ServiceWithInstanceNameOfTraceStatusCount() {

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServiceInstanceNameWithTraceStatusCount> getSinwtscList() {
        return sinwtscList;
    }

    public void setSinwtscList(List<ServiceInstanceNameWithTraceStatusCount> sinwtscList) {
        this.sinwtscList = sinwtscList;
    }
}
