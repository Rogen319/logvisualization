package escore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ESCoreScheduledTasks {
    @Autowired
    ESCoreScheduledService service;

    //Delete the useless span info: istio-telemetry and istio-mixer every 10 seconds
    @Scheduled(initialDelay = 36000, fixedDelay = 10000)
    public void deleteUselessSpanInfo(){
        service.deleteUselessSpanInfo();
    }

    //Update pod info every 30 minutes  30000  1800000
    @Scheduled(initialDelay = 9000, fixedDelay = 60000)
    public void updatePodInfo(){
        service.updatePodInfo();
    }

    //Update node info every 1 hour 30000 3600000
    @Scheduled(initialDelay = 18000, fixedDelay = 60000)
    public void updateNodeInfo(){
        service.updateNodeInfo();
    }

    //Update request traceid relation every 9 seconds
//    @Scheduled(initialDelay = 27000, fixedDelay = 9000)
//    public void updateRelationInfo(){
//        service.updateRelationInfo();
//    }

}
