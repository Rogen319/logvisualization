package algrithm.sequence.service.impl;

import algrithm.sequence.domain.LogItem;
import algrithm.sequence.domain.SequenceInfo;
import algrithm.sequence.domain.SequenceTypeDetails;
import algrithm.sequence.dto.AsynRequestDto;
import algrithm.sequence.dto.LogDto;
import algrithm.sequence.dto.RequestTypeSequenceDto;
import algrithm.sequence.dto.TraceTypeSequenceDto;
import algrithm.sequence.repository.SequenceRepository;
import algrithm.sequence.service.SequenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SequenceServiceImpl implements SequenceService {

    @Autowired
    private SequenceRepository sequenceRepository;

    private Comparator<LogItem> logItemComparator = Comparator.comparing(LogItem::getTimestamp);

    private static Logger logger = LoggerFactory.getLogger(SequenceServiceImpl.class);

    @Override
    public TraceTypeSequenceDto getSequence(AsynRequestDto requestDto) {

        Map<String, String> map = sequenceRepository.getTraceIdByRequestTypeAndTimeRange(requestDto);

        Set<String> traceIds = map.keySet();
        SequenceInfo sequenceInfo = new SequenceInfo();

        List<SequenceTypeDetails> sequenceTypeDetailsList = new ArrayList<>();
        List<List<String>> allSequences = new ArrayList<>();
        traceIds.forEach(t -> {
            String traceStatus = map.get(t);
            StringBuilder sb = new StringBuilder("http://logvisualization-logapi:16319" + "/getLogByTraceId/");
            sb.append(t).append("/0");

            RestTemplate restTemplate = new RestTemplate();

            LogDto logResp = restTemplate.getForObject(sb.toString(), LogDto.class);
            List<LogItem> logItems = logResp.getLogs();

            Set<String> services = logItems.stream().filter(m -> m.getLogType
                    ().equals("InvocationRequest")).map(m -> m.getServiceInfo
                    ().getServiceName()).collect(Collectors.toSet());

            // 判断是否为同一个traceType
            if (services.equals(requestDto.getServices())) {
                // 获取sequence
                List<String> sequence = logItems.stream().filter(m -> m.getLogType().equals("InvocationRequest"))
                        .sorted(logItemComparator).map(m -> m.getServiceInfo().getServiceName())
                        .collect(Collectors.toList());
                logger.info("tranceId: {}, sequence: {}", t, sequence.toString());
                allSequences.add(sequence);
                sequenceTypeDetailsList.forEach(m -> {
                    if (m.getSequence().equals(sequence)) {
                        if ("true".equals(traceStatus)) {
                            m.setSuccessTime(m.getSuccessTime() + 1);
                        } else {
                            m.setFailedTime(m.getFailedTime() + 1);
                        }
                        m.getTraceSet().add(t);
                    } else {
                        SequenceTypeDetails details = new SequenceTypeDetails();
                        if ("true".equals(traceStatus)) {
                            m.setSuccessTime(1L);
                        } else {
                            m.setFailedTime(1L);
                        }
                        details.setSequence(sequence);
                        details.getTraceSet().add(t);
                        sequenceTypeDetailsList.add(details);
                    }
                });
            }
        });

        List<SequenceInfo> sequenceInfos = new ArrayList<>();
        sequenceTypeDetailsList.forEach(i -> {
            SequenceInfo info = new SequenceInfo(i.getSequence(), i.getTraceSet(), i.getFailedTime() / (i
                    .getSuccessTime() + i.getFailedTime()));
            sequenceInfos.add(info);
        });

        Set<String> asynServices = analyseAsyn(allSequences, requestDto.getServices());

        TraceTypeSequenceDto dto = new TraceTypeSequenceDto();
        dto.setAsyn(!asynServices.isEmpty());
        dto.setAsynService(asynServices);
        dto.setSequences(sequenceInfos);
        dto.setStatus(true);
        dto.setMessage("success");

        return dto;
    }


    private Set<String> analyseAsyn(List<List<String>> allSvcSeqInTraceType,
                                    Set<String> service) {
        Set<String> asynService = new HashSet<>();
        service.forEach(s -> {
            Set<Integer> set = new HashSet<>();
            allSvcSeqInTraceType.forEach(e -> {
                set.add(e.indexOf(s));
            });
            if (set.size() != 1) {
                asynService.add(s);
            }
        });
        return asynService;
    }
}
