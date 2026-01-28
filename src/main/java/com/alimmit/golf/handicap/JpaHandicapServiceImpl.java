package com.alimmit.golf.handicap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alimmit.golf.scorecard.ScorecardService;

import java.util.Optional;

@Service
class JpaHandicapServiceImpl implements HandicapService {

  private final ScorecardService scorecardService;
  private final HandicapCalculator handicapCalculator;
  private final HandicapMapper handicapMapper;
  private final HandicapRepository handicapRepository;

  JpaHandicapServiceImpl(
      ScorecardService scorecardService,
      HandicapCalculator handicapCalculator,
      HandicapMapper handicapMapper,
      HandicapRepository handicapRepository) {
    this.scorecardService = scorecardService;
    this.handicapCalculator = handicapCalculator;
    this.handicapMapper = handicapMapper;
    this.handicapRepository = handicapRepository;
  }

  @Override
  @Transactional
  public void calculate(String userId) {
    handicapCalculator.calculate(scorecardService.listAll(userId))
        .ifPresent(handicapDto -> handicapRepository.save(handicapMapper.toEntity(handicapDto, userId)));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HandicapDto> getHandicap() {
    return handicapMapper.toOptionalDto(handicapRepository.findHandicapForCurrentUser());
  }
}
