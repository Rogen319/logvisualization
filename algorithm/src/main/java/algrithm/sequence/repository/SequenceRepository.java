package algrithm.sequence.repository;

import algrithm.sequence.dto.AsynRequestDto;
import algrithm.sequence.dto.TraceIdsRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Repository
public class SequenceRepository {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate restTemplate;

    public Map<String, String> getTraceIdByRequestTypeAndTimeRange(AsynRequestDto dto) {
        String url = "http://logvisualization-escore/traceIds";
        logger.info("Escore request URL: {}", url);

        TraceIdsRequestDto request = new TraceIdsRequestDto(dto.getRequestType(), dto.getEndTime(), dto.getLookback());
        HttpEntity<TraceIdsRequestDto> httpEntity = new HttpEntity<>(request);
        return restTemplate.exchange(url, HttpMethod.POST, httpEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                }).getBody();
    }
}
