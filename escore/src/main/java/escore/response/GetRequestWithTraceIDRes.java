package escore.response;

import escore.bean.RequestWithTraceInfo;
import escore.bean.ServiceWithTraceStatusCount;

import java.util.List;

public class GetRequestWithTraceIDRes extends GeneralResponse {
    private List<RequestWithTraceInfo> requestWithTraceInfoList;
    private List<ServiceWithTraceStatusCount> serviceWithTraceStatusCountList;

    public GetRequestWithTraceIDRes() {
        super();
    }

    public List<RequestWithTraceInfo> getRequestWithTraceInfoList() {
        return requestWithTraceInfoList;
    }

    public void setRequestWithTraceInfoList(List<RequestWithTraceInfo> requestWithTraceInfoList) {
        this.requestWithTraceInfoList = requestWithTraceInfoList;
    }

    public List<ServiceWithTraceStatusCount> getServiceWithTraceStatusCountList() {
        return serviceWithTraceStatusCountList;
    }

    public void setServiceWithTraceStatusCountList(List<ServiceWithTraceStatusCount> serviceWithTraceStatusCountList) {
        this.serviceWithTraceStatusCountList = serviceWithTraceStatusCountList;
    }
}
