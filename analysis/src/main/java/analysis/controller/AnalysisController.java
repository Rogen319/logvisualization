package analysis.controller;

import analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @auther lwh
 * @create 2019-01-16 14:57
 */
@RestController
public class AnalysisController {
    @Autowired
    private AnalysisService service;

    //Improve service deployment according to the current system log
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/api/improveDeployment", method = RequestMethod.GET)
    public boolean improveDeployment() {
        return service.improveDeployment();
    }
}
