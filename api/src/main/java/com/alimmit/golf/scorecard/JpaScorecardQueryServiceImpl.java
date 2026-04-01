package com.alimmit.golf.scorecard;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class JpaScorecardQueryServiceImpl implements ScorecardQueryService {

  private final ScorecardRepository scorecardRepository;
  private final ScorecardMapper scorecardMapper;

  JpaScorecardQueryServiceImpl(
      ScorecardRepository scorecardRepository, ScorecardMapper scorecardMapper) {
    this.scorecardRepository = scorecardRepository;
    this.scorecardMapper = scorecardMapper;
  }

  @Override
  public List<ScorecardDto> findMostRecentForUser(String userId, int limit) {
    return scorecardRepository.findMostRecentForUser(userId, PageRequest.of(0, limit)).stream()
        .map(scorecardMapper::toDto)
        .toList();
  }
}
