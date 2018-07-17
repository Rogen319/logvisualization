package logapi.service;

import logapi.response.GetLogByTraceIdRes;

public interface LogAPIService {
    GetLogByTraceIdRes getLogByTraceId(String traceId);
}
