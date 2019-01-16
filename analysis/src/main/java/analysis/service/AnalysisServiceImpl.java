package analysis.service;

import analysis.response.ClusterResult;
import analysis.response.GetNodesListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @auther lwh
 * @create 2019-01-16 15:23
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String LOOKBACK = "86400000";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public boolean improveDeployment() {
        //查询目前集群的节点信息
        GetNodesListResponse result = restTemplate.getForObject(
                "http://logvisualization-k8sapi:18319/api/getNodesList",
                GetNodesListResponse.class);
        if (result.isStatus()) {
            int numberOfCluster = result.getNodes().size() - 1;
            log.info(String.format("The current worker node number is %d", numberOfCluster));
            //调用谱聚类算法进行服务依赖的分块
            String endTs = String.valueOf(System.currentTimeMillis());
            String getClusterUrl = String.format("http://logvisualization-algorithm:18888/cluster?endTs=%s&lookback=%s&k=%d", endTs, LOOKBACK, numberOfCluster);
            ClusterResult clusterResult = restTemplate.getForObject(getClusterUrl, ClusterResult.class);
            log.info(String.format("The cluster result is %s", clusterResult.getClusters().toString()));
        }
        return false;
    }
}
