package logapi.controller;

import logapi.request.GetLogByInstanceNameAndTraceIdReq;
import logapi.response.GetLogByInstanceNameAndTraceIdRes;
import logapi.response.LogResponse;
import logapi.service.LogAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogAPIController {
    @Autowired
    LogAPIService service;

    //Get the log corresponding to the trace id
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getLogByTraceId/{traceId}/{flag}", method ={RequestMethod.GET})
    public LogResponse getLogByTraceId(@PathVariable String traceId, @PathVariable int flag){
        return service.getLogByTraceId(traceId, flag);
    }

    //Get the log corresponding to the instance name(pod)
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getLogByInstanceName/{instanceName}", method ={RequestMethod.GET})
    public LogResponse getLogByInstanceName(@PathVariable String instanceName){
        return service.getLogByInstanceName(instanceName);
    }

    //Get the log corresponding to the instance name(pod) and trace id
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getLogByInstanceNameAndTraceId", method ={RequestMethod.POST})
    public GetLogByInstanceNameAndTraceIdRes getLogByInstanceNameAndTraceId(@RequestBody GetLogByInstanceNameAndTraceIdReq request){
        return service.getLogByInstanceNameAndTraceId(request);
    }
}
