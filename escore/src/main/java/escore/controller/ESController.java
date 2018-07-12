package escore.controller;

import escore.response.GetRequestTypesRes;
import escore.response.GetRequestWithTraceIDRes;
import escore.service.ESCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ESController {

    @Autowired
    ESCoreService service;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/demo", method ={RequestMethod.GET})
    public String demo(){
        return service.demo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getRequestTypes", method ={RequestMethod.GET})
    public GetRequestTypesRes getRequestTypes(){
        return service.getRequestTypes();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getRequestWithTraceID", method ={RequestMethod.GET})
    public GetRequestWithTraceIDRes getRequestWithTraceID(){
        return service.getRequestWithTraceID();
    }
}
