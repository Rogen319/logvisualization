package algrithm.sequence.service;

import algrithm.sequence.dto.RequestTypeSequenceDto;

import java.util.List;

public interface SequenceService {
    List<RequestTypeSequenceDto> getSequence(long endTime, long lookback);
}
