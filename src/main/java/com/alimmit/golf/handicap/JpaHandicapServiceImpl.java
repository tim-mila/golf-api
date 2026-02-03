package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class JpaHandicapServiceImpl implements HandicapService {

  private final HandicapCalculator handicapCalculator;
  private final HandicapMapper handicapMapper;
  private final HandicapRepository handicapRepository;

  JpaHandicapServiceImpl(
      HandicapCalculator handicapCalculator,
      HandicapMapper handicapMapper,
      HandicapRepository handicapRepository) {
    this.handicapCalculator = handicapCalculator;
    this.handicapMapper = handicapMapper;
    this.handicapRepository = handicapRepository;
  }

  @Override
  @Transactional
  public void calculate(List<ScorecardDto> scorecards, String golferId) {
    handicapCalculator.calculate(scorecards)
        .ifPresent(handicapDto -> handicapRepository.save(handicapMapper.toEntity(handicapDto, golferId)));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HandicapDto> getHandicap() {
    return handicapMapper.toOptionalDto(handicapRepository.findHandicapForCurrentUser());
  }
}
