package escore.response;

import escore.bean.ServiceWithInstanceOfTraceStatusCount;

import java.util.List;

public class ServiceWithInstanceOfTSCRes extends GeneralResponse {
    private List<ServiceWithInstanceOfTraceStatusCount> list;

    public ServiceWithInstanceOfTSCRes() {

    }

    public List<ServiceWithInstanceOfTraceStatusCount> getList() {
        return list;
    }

    public void setList(List<ServiceWithInstanceOfTraceStatusCount> list) {
        this.list = list;
    }
}
