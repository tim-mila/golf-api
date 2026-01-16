package com.alimmit.golf.scorecard;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ScorecardService {
    ScorecardDto create(ScorecardRequestDto request);

    @Transactional(readOnly = true)
    List<ScorecardDto> listAll();

    @Transactional(readOnly = true)
    Optional<ScorecardDto> getById(String id);

    void deleteById(String id);
}
