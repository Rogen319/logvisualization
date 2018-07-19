package escore.service;

import escore.bean.NodeInfo;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;
import escore.response.QueryNodeInfoRes;
import escore.response.QueryPodInfoRes;

public interface ESCoreService {
    String demo();
//    GetRequestTypesRes getRequestTypes();
    GetRequestWithTraceIDRes getRequestWithTraceID();
    GetRequestWithTraceIDRes getRequestWithTraceIDByTimeRange(GetRequestWithTraceIDByTimeRangeReq request);
    QueryPodInfoRes queryPodInfo(String podName);
    QueryNodeInfoRes queryNodeInfo(NodeInfo nodeInfo);
}
