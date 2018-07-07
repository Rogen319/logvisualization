package k8sapi.controller;

import k8sapi.response.GetNodesListResponse;
import k8sapi.response.GetPodsListResponse;
import k8sapi.service.KubernetesAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class KubernetesAPIController {
    @Autowired
    KubernetesAPIService service;

    //Get the list of all current nodes info
    @CrossOrigin(origins = "*")
    @RequestMapping(value="/api/getNodesList", method= RequestMethod.GET)
    public GetNodesListResponse getNodesList(){
        return service.getNodesList();
    }

    //Get the list of all current pods info
    @CrossOrigin(origins = "*")
    @RequestMapping(value="/api/getPodsList", method= RequestMethod.GET)
    public GetPodsListResponse getPodsList(){
        return service.getPodsListAPI();
    }

}
