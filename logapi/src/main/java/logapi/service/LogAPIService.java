package logapi.service;

import logapi.request.GetLogByInstanceNameAndTraceIdReq;
import logapi.response.GetLogByServiceNameAndTraceIdRes;
import logapi.response.LogResponse;

public interface LogAPIService {
    LogResponse getLogByTraceId(String traceId, int flag);

    LogResponse getLogByInstanceName(String instanceName);

    GetLogByServiceNameAndTraceIdRes getLogByServiceNameAndTraceId(GetLogByInstanceNameAndTraceIdReq request);
}
