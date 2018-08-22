package algrithm.sequence.service.impl;

import algrithm.sequence.domain.LogItem;
import algrithm.sequence.domain.RequestWithTraceInfo;
import algrithm.sequence.domain.TraceInfo;
import algrithm.sequence.domain.TraceType;
import algrithm.sequence.dto.GetRequestWithTraceIDRes;
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

@Service
public class SequenceServiceImpl implements SequenceService {
    private static final Logger logger = LoggerFactory.getLogger(SequenceServiceImpl.class);

    @Autowired
    private SequenceRepository sequenceRepository;

    @Override
    public List<RequestTypeSequenceDto> getSequence(long endTime, long lookback) {
        GetRequestWithTraceIDRes ret = sequenceRepository.getRequestWithTraceIDRes(endTime, lookback);

        List<RequestTypeSequenceDto> requestTypeSequenceDtos = new ArrayList<>();
        for (RequestWithTraceInfo rti : ret.getRequestWithTraceInfoList()) {
            RequestTypeSequenceDto rtsd = new RequestTypeSequenceDto();
            rtsd.setRequestTypeName(rti.getRequestType());
            List<TraceTypeSequenceDto> traceTypeSequenceDtos = new ArrayList<>();

            for (TraceType tt : rti.getTraceTypeList()) {
                TraceTypeSequenceDto ttsd = new TraceTypeSequenceDto();
                ttsd.setTraceType(tt.getTypeName());
                List<List<String>> allSvcSeqInTraceType = new ArrayList<>();

                for (TraceInfo ti : tt.getTraceInfoList()) {
                    List<String> svcSeq = getServiceSequence(ti.getTraceId());
                    logger.info("traceId: {}, service sequence: {}", ti.getTraceId(), svcSeq.toString());
                    allSvcSeqInTraceType.add(svcSeq);
                }

                Set<String> service = tt.getTraceInfoList().get(0).getServiceList();
                List<String> asynScope = analyseAsyn(allSvcSeqInTraceType, service);
                ttsd.setAsynService(asynScope);
                if (asynScope.size() == 0) {
                    ttsd.setAsyn(false);
                } else {
                    ttsd.setAsyn(true);
                }

                traceTypeSequenceDtos.add(ttsd);
            }
            rtsd.setTraceTypes(traceTypeSequenceDtos);
            requestTypeSequenceDtos.add(rtsd);
        }

        return requestTypeSequenceDtos;
    }

    private Comparator<LogItem> logItemComparator = (o1, o2) -> {

        if (o1.getTimestamp().equals(o2.getTimestamp())) {
            if (o1.getLogType().equals("InvocationRequest"))
                return -1;
            else
                return 1;
        }
        return o1.getTimestamp().compareTo(o2.getTimestamp());

    };

    private List<String> getServiceSequence(String traceId) {
        StringBuilder sb =
                new StringBuilder("http://logvisualization-logapi:16319/getLogByTraceId/");
        sb.append(traceId);
        sb.append("/0");

        RestTemplate restTemplate = new RestTemplate();
        LogDto logResp =
                restTemplate.getForObject(sb.toString(), LogDto.class);
        List<LogItem> logItems = logResp.getLogs();
        LogItem[] logItemArr = logItems.toArray(new LogItem[logItems.size()]);
        Arrays.sort(logItemArr, logItemComparator);

        List<String> svcSeq = new ArrayList<>();
        for (LogItem li : logItemArr) {
            if (li.getLogType().equals("InvocationRequest") || li.getLogType().equals("InvocationResponse"))
                svcSeq.add(li.getServiceInfo().getServiceName());
        }

        return svcSeq;
    }

    private List<String> analyseAsyn(List<List<String>> allSvcSeqInTraceType, Set<String> service) {
        List<String> asynService = new ArrayList<>();
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
