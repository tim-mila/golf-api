package com.alimmit.golf.scorecard;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScorecardService {
    ScorecardDto create(ScorecardRequestDto request);

    @Transactional(readOnly = true)
    List<ScorecardDto> listAll();

    @Transactional(readOnly = true)
    ScorecardDto getById(String id);

    void deleteById(String id);
}
