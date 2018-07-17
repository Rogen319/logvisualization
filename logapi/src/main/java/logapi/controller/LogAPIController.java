package logapi.controller;

import logapi.response.GetLogByTraceIdRes;
import logapi.service.LogAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LogAPIController {
    @Autowired
    LogAPIService service;

    //Get the dependencies from all of the stored record
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getLogByTraceId/{traceId}", method ={RequestMethod.GET})
    public GetLogByTraceIdRes getLogByTraceId(@PathVariable String traceId){
        return service.getLogByTraceId(traceId);
    }
}
