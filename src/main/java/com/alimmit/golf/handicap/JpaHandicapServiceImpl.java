package com.alimmit.golf.handicap;

import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.scorecard.ScorecardDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // handicap may not be calculated based on number of holes played
    handicapCalculator
        .calculate(scorecards)
        .ifPresent(
            handicapDto -> {
              // Update existing or create new handicap
              HandicapEntity handicap =
                  handicapRepository
                      .findHandicapForUser(golferId)
                      .map(entity -> handicapMapper.updateEntity(entity, handicapDto))
                      .orElse(handicapMapper.toEntity(handicapDto, golferId));
              handicapRepository.save(handicap);
            });
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<HandicapDto> getHandicap() {
    return handicapMapper.toOptionalDto(handicapRepository.findHandicapForCurrentUser());
  }

  @Override
  public List<HandicapRevisionDto> getHistory() {
    HandicapEntity handicap =
        handicapRepository.findHandicapForCurrentUser().orElseThrow(NotFoundException::new);
    Revisions<Integer, HandicapEntity> revisions =
        handicapRepository.findRevisions(handicap.getHandicapId());
    return revisions.stream().map(handicapMapper::toRevision).toList();
  }
}
