package escore.service;

import escore.bean.NodeInfo;
import escore.request.*;
import escore.response.*;

import java.util.List;

public interface ESCoreService {
    String demo();
    GetRequestWithTraceIDRes getRequestWithTraceIDByTimeRange(GetRequestWithTraceIDByTimeRangeReq request);
    QueryPodInfoRes queryPodInfo(String podName);
    QueryNodeInfoRes queryNodeInfo(NodeInfo nodeInfo);
    GetInstanceNamesFromESRes getInstanceNamesOfSpecifiedService(String serviceName);
    List<TraceSequenceRes> getSequenceInfo();
    ServiceWithTraceCountRes getServiceWithTraceCountByRequestType(GetServiceWithTraceCountByRequestTypeReq request);
    ServiceWithTraceCountRes getServiceWithTraceCountByTraceType(GetServiceWithTraceCountByTraceTypeReq request);
    ServiceWithInstanceOfTSCRes getServiceWithInstanceOfTSCByRequestType(GetServiceWithInstanceOfTSCByRequestTypeReq request);
    ServiceWithInstanceOfTSCRes getServiceWithInstanceOfTSCByTraceType( GetServiceWithInstanceOfTSCByTraceTypeReq request);
}
