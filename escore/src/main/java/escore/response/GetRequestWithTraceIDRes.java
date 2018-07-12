package escore.response;

import escore.bean.RequestWithTraceID;

import java.util.List;

public class GetRequestWithTraceIDRes extends GeneralResponse{
    private List<RequestWithTraceID> requestWithTraceIDList;

    public GetRequestWithTraceIDRes(){
        super();
    }

    public List<RequestWithTraceID> getRequestWithTraceIDList() {
        return requestWithTraceIDList;
    }

    public void setRequestWithTraceIDList(List<RequestWithTraceID> requestWithTraceIDList) {
        this.requestWithTraceIDList = requestWithTraceIDList;
    }
}
