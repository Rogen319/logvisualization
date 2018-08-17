package escore.response;

import escore.bean.ServiceWithTraceStatusCount;

import java.util.List;

public class ServiceWithTraceCountRes extends GeneralResponse {
    private List<ServiceWithTraceStatusCount> serviceWithTraceStatusCountList;

    public ServiceWithTraceCountRes(){

    }

    public List<ServiceWithTraceStatusCount> getServiceWithTraceStatusCountList() {
        return serviceWithTraceStatusCountList;
    }

    public void setServiceWithTraceStatusCountList(List<ServiceWithTraceStatusCount> serviceWithTraceStatusCountList) {
        this.serviceWithTraceStatusCountList = serviceWithTraceStatusCountList;
    }
}
