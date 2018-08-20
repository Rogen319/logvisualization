package escore.bean;

import java.util.List;

public class ServiceWithInstanceOfTraceStatusCount {
    private String serviceName;
    private List<ServiceInstanceWithTraceStatusCount> siwtscList;

    public ServiceWithInstanceOfTraceStatusCount(){

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<ServiceInstanceWithTraceStatusCount> getSiwtscList() {
        return siwtscList;
    }

    public void setSiwtscList(List<ServiceInstanceWithTraceStatusCount> siwtscList) {
        this.siwtscList = siwtscList;
    }
}
