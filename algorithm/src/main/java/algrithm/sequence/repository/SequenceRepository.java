package algrithm.sequence.repository;

import algrithm.sequence.config.EscoreConfig;
import algrithm.sequence.dto.GetRequestWithTraceIDRes;
import algrithm.sequence.dto.TimeRangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class SequenceRepository {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EscoreConfig escoreConfig;

    public GetRequestWithTraceIDRes getRequestWithTraceIDRes(long endTime, long lookback) {
        StringBuilder sb = new StringBuilder("http://");
        sb.append(escoreConfig.getIp());
        sb.append(":");
        sb.append(escoreConfig.getPort());
        sb.append("/getRequestWithTraceIDByTimeRange");
        logger.info("Escore request URL: {}", sb.toString());

        TimeRangeRequest body = new TimeRangeRequest(endTime, lookback);

        return restTemplate.postForObject(sb.toString(), body, GetRequestWithTraceIDRes.class);
    }
}
