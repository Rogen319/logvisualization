package algrithm.sequence.service.impl;

import algrithm.sequence.domain.LogItem;
import algrithm.sequence.domain.SequenceInfo;
import algrithm.sequence.domain.SequenceTypeDetails;
import algrithm.sequence.dto.AsynRequestDto;
import algrithm.sequence.dto.LogDto;
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

        final Set<String> traceIds = map.keySet();
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

                SequenceTypeDetails details = new SequenceTypeDetails();
                details.setSequence(sequence);

                int index = sequenceTypeDetailsList.indexOf(details);
                if (sequenceTypeDetailsList.isEmpty() || index == -1) {
                    if ("true".equals(traceStatus)) {
                        details.setSuccessTime(1L);
                    } else {
                        details.setFailedTime(1L);
                    }
                    details.getTraceSet().add(t);
                    sequenceTypeDetailsList.add(details);
                } else {
                    SequenceTypeDetails tem = sequenceTypeDetailsList.get(index);
                    if ("true".equals(traceStatus)) {
                        tem.setSuccessTime(tem.getSuccessTime() + 1);
                    } else {
                        tem.setFailedTime(tem.getFailedTime() + 1);
                    }
                    details.getTraceSet().add(t);
                }
            }
        });

        if (sequenceTypeDetailsList.isEmpty()) {
            TraceTypeSequenceDto dto = new TraceTypeSequenceDto();
            dto.setAsyn(false);
            dto.setStatus(false);
            dto.setMessage("No such trace type, contains service:" + requestDto.getServices().toString());

            return dto;
        }
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


    private Set<String> analyseAsyn(final List<List<String>> allSvcSeqInTraceType,
                                    final Set<String> services) {

        int sequenceSize = allSvcSeqInTraceType.get(0).size();
        int sequenceCount = allSvcSeqInTraceType.size();

        int[] ret = new int[sequenceSize];
        List<String> compareSeq = allSvcSeqInTraceType.get(0);
        for (int i = 0; i < sequenceSize; i++) {
            for (int j = 0; j < sequenceCount; j++) {
                if (!allSvcSeqInTraceType.get(j).get(i).equals(compareSeq.get(i))) {
                    ret[i]++;
                }
            }
        }

        Set<Integer> asynIndex = new HashSet<>();
        Set<String> syncSvc = new HashSet<>();
        for (int i = 0; i < sequenceSize; i++) {
            if (0 == ret[i]) {
                syncSvc.add(compareSeq.get(i));
            } else {
                asynIndex.add(i);
            }
        }

        Set<String> tempAsyn = new HashSet<>();
        syncSvc.forEach(s -> {
            allSvcSeqInTraceType.forEach(l -> {
                asynIndex.forEach(e -> {
                    if (s.equals(l.get(e))) {
                        tempAsyn.add(s);
                    }
                });
            });
        });
        syncSvc.removeAll(tempAsyn);
//        service.forEach(s -> {
//            Set<Integer> set = new HashSet<>();
//            allSvcSeqInTraceType.forEach(e -> {
//                set.add(e.indexOf(s));
//            });
//            if (set.size() != 1) {
//                asynService.add(s);
//            }
//        });

        Set<String> asynSvc = new HashSet<>();
        asynSvc.addAll(services);
        asynSvc.removeAll(syncSvc);
        return asynSvc;
    }
}
