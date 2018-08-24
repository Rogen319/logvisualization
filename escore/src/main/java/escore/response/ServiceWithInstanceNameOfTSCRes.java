package escore.response;

import escore.bean.ServiceWithInstanceNameOfTraceStatusCount;

import java.util.List;

public class ServiceWithInstanceNameOfTSCRes extends GeneralResponse{
    private List<ServiceWithInstanceNameOfTraceStatusCount> list;

    public ServiceWithInstanceNameOfTSCRes(){

    }

    public List<ServiceWithInstanceNameOfTraceStatusCount> getList() {
        return list;
    }

    public void setList(List<ServiceWithInstanceNameOfTraceStatusCount> list) {
        this.list = list;
    }
}
