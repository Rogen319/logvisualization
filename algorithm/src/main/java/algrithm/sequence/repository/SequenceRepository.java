package algrithm.sequence.repository;

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

    public GetRequestWithTraceIDRes getRequestWithTraceIDRes(long endTime, long lookback) {
        String url = "http://logvisualization-escore:17319/getRequestWithTraceIDByTimeRange";
        logger.info("Escore request URL: {}", url);

        TimeRangeRequest body = new TimeRangeRequest(endTime, lookback);

        return restTemplate.postForObject(url, body, GetRequestWithTraceIDRes.class);
    }
}
