package algrithm.sequence.service;

import algrithm.sequence.dto.AsynRequestDto;
import algrithm.sequence.dto.RequestTypeSequenceDto;
import algrithm.sequence.dto.TraceTypeSequenceDto;

import java.util.List;

public interface SequenceService {
    TraceTypeSequenceDto getSequence(AsynRequestDto requestDto);
}
