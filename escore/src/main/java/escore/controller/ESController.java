package escore.controller;

import escore.bean.NodeInfo;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.response.GetRequestWithTraceIDRes;
import escore.response.QueryNodeInfoRes;
import escore.response.QueryPodInfoRes;
import escore.service.ESCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ESController {

    @Autowired
    ESCoreService service;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/demo", method ={RequestMethod.GET})
    public String demo(){
        return service.demo();
    }

//    @CrossOrigin(origins = "*")
//    @RequestMapping(value = "/getRequestTypes", method ={RequestMethod.GET})
//    public GetRequestTypesRes getRequestTypes(){
//        return service.getRequestTypes();
//    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getRequestWithTraceID", method ={RequestMethod.GET})
    public GetRequestWithTraceIDRes getRequestWithTraceID(){
        return service.getRequestWithTraceID();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getRequestWithTraceIDByTimeRange", method ={RequestMethod.POST})
    public GetRequestWithTraceIDRes getRequestWithTraceIDByTimeRange(@RequestBody GetRequestWithTraceIDByTimeRangeReq request){
        return service.getRequestWithTraceIDByTimeRange(request);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/queryPodInfo/{podName}", method ={RequestMethod.GET})
    public QueryPodInfoRes queryPodInfo(@PathVariable String podName){
        return service.queryPodInfo(podName);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value="/queryNodeInfo", method= RequestMethod.POST)
    public QueryNodeInfoRes queryNodeInfo(@RequestBody NodeInfo nodeInfo){
        return service.queryNodeInfo(nodeInfo);
    }
}
