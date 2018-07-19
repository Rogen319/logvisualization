package logapi.service;

import logapi.response.LogResponse;

public interface LogAPIService {
    LogResponse getLogByTraceId(String traceId);
    LogResponse getLogByInstanceName(String instanceName);
}
