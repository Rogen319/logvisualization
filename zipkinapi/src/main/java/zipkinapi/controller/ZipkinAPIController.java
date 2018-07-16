package zipkinapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import zipkinapi.response.GetDependenciesResponse;
import zipkinapi.service.ZipkinAPIService;

@RestController
public class ZipkinAPIController {
    @Autowired
    ZipkinAPIService service;

    //Get the dependencies from all of the stored record
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getDependencies", method ={RequestMethod.GET})
    public GetDependenciesResponse getDependencies(){
        return service.getDependencies();
    }
}
