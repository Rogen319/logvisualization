package escore.service;

import escore.bean.NodeInfo;
import escore.request.*;
import escore.response.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Map<String, String> getTraceIdsByRequestType(GetTraceIdsByRequestTypeAndTimeRange request);
    ServiceWithInstanceNameOfTSCRes getServiceWithInstanceNameOfTSCByRequestType(GetServiceWithInstanceOfTSCByRequestTypeReq request);
    ServiceWithInstanceNameOfTSCRes getServiceWithInstanceNameOfTSCByTraceType( GetServiceWithInstanceOfTSCByTraceTypeReq request);
}
