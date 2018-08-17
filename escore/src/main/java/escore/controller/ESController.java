package escore.controller;

import escore.bean.NodeInfo;
import escore.request.GetRequestWithTraceIDByTimeRangeReq;
import escore.request.GetServiceWithTraceCountByRequestTypeReq;
import escore.request.GetServiceWithTraceCountByTraceTypeReq;
import escore.response.*;
import escore.service.ESCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getInstanceNamesOfSpecifiedService/{serviceName}", method ={RequestMethod.GET})
    public GetInstanceNamesFromESRes getInstanceNamesOfSpecifiedService(@PathVariable String serviceName){
        return service.getInstanceNamesOfSpecifiedService(serviceName);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/sequences")
    public List<TraceSequenceRes> getSequenceInfo(){
        return service.getSequenceInfo();
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getServiceWithTraceCountByRequestType", method ={RequestMethod.POST})
    public ServiceWithTraceCountRes getServiceWithTraceCountByRequestType(@RequestBody GetServiceWithTraceCountByRequestTypeReq request){
        return service.getServiceWithTraceCountByRequestType(request);
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getServiceWithTraceCountByTraceType", method ={RequestMethod.POST})
    public ServiceWithTraceCountRes getServiceWithTraceCountByTraceType(@RequestBody GetServiceWithTraceCountByTraceTypeReq request){
        return service.getServiceWithTraceCountByTraceType(request);
    }
}
