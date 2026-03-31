package com.alimmit.golf.scorecard;

import java.util.List;
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
  public List<ScorecardDto> findAllForUser(String userId) {
    return scorecardRepository.findAllCreatedBy(userId).stream()
        .map(scorecardMapper::toDto)
        .toList();
  }
}
