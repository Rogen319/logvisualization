package escore.service;

import escore.bean.NodeInfo;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.*;

import java.util.List;

public interface ESCoreService {
    String demo();
    GetRequestWithTraceIDRes getRequestWithTraceIDByTimeRange(GetRequestWithTraceIDByTimeRangeReq request);
    QueryPodInfoRes queryPodInfo(String podName);
    QueryNodeInfoRes queryNodeInfo(NodeInfo nodeInfo);
    GetInstanceNamesFromESRes getInstanceNamesOfSpecifiedService(String serviceName);
    List<TraceSequenceRes> getSequenceInfo();
}
