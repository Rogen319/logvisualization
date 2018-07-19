package logapi.service;

import logapi.request.GetLogByInstanceNameAndTraceIdReq;
import logapi.response.GetLogByInstanceNameAndTraceIdRes;
import logapi.response.LogResponse;

public interface LogAPIService {
    LogResponse getLogByTraceId(String traceId);
    LogResponse getLogByInstanceName(String instanceName);
    GetLogByInstanceNameAndTraceIdRes getLogByInstanceNameAndTraceId(GetLogByInstanceNameAndTraceIdReq request);
}
