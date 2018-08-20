package algrithm.sequence.dto;

import algrithm.sequence.domain.RequestWithTraceInfo;

import java.util.List;

public class GetRequestWithTraceIDRes extends GeneralResponse {
    private List<RequestWithTraceInfo> requestWithTraceInfoList;

    public GetRequestWithTraceIDRes() {
        super();
    }

    public List<RequestWithTraceInfo> getRequestWithTraceInfoList() {
        return requestWithTraceInfoList;
    }

    public void setRequestWithTraceInfoList(List<RequestWithTraceInfo> requestWithTraceInfoList) {
        this.requestWithTraceInfoList = requestWithTraceInfoList;
    }
}
