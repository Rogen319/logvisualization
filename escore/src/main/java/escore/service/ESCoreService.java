package escore.service;

import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;

public interface ESCoreService {
    String demo();
    GetRequestTypesRes getRequestTypes();
    GetRequestWithTraceIDRes getRequestWithTraceID();
}
