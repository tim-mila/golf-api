package com.alimmit.golf.scorecard;

import com.alimmit.golf.differential.Differential;
import com.alimmit.golf.differential.DifferentialCalculator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service layer for scorecard operations. Handles business logic and transaction management. */
@Service
@Transactional
class JpaScorecardServiceImpl implements ScorecardService {

  private final ScorecardRepository scorecardRepository;
  private final ScorecardMapper scorecardMapper;
  private final DifferentialCalculator differentialCalculator;

  JpaScorecardServiceImpl(
      ScorecardRepository scorecardRepository,
      ScorecardMapper scorecardMapper,
      DifferentialCalculator differentialCalculator) {
    this.scorecardRepository = scorecardRepository;
    this.scorecardMapper = scorecardMapper;
    this.differentialCalculator = differentialCalculator;
  }

  /**
   * Create a new scorecard. Generates ID and persists to database with audit fields. If the rating
   * and slope are not provided, they are looked up from the golf course API.
   */
  @Override
  public ScorecardDto create(ScorecardRequestDto request) {
    Differential differential = differentialCalculator.calculateDifferential(request);
    ScorecardEntity saved =
        scorecardRepository.save(
            scorecardMapper.toEntity(
                request, differential.differential(), differential.indexEstablished()));
    return scorecardMapper.toDto(saved);
  }

  /** List scorecards for the current authenticated user using keyset pagination. */
  @Transactional(readOnly = true)
  @Override
  public ScorecardPageDto list(int limit, ScorecardCursor cursor) {
    // Fetch one extra row to determine whether a next page exists
    Limit limitPlusOne = Limit.of(limit + 1);
    List<ScorecardEntity> results =
        cursor == null
            ? scorecardRepository.findFirstPageForCurrentUser(limitPlusOne)
            : scorecardRepository.findNextPageForCurrentUser(
                cursor.scoreDate(), cursor.scorecardId(), limitPlusOne);

    boolean hasNext = results.size() > limit;
    List<ScorecardEntity> page = hasNext ? results.subList(0, limit) : results;

    String nextCursor =
        hasNext
            ? new ScorecardCursor(page.getLast().getScoreDate(), page.getLast().getId()).encode()
            : null;

    List<ScorecardDto> data = page.stream().map(scorecardMapper::toDto).toList();
    return new ScorecardPageDto(data, nextCursor);
  }

  /** Get a single scorecard by ID for the current authenticated user. */
  @Transactional(readOnly = true)
  @Override
  public Optional<ScorecardDto> getById(UUID id) {
    return scorecardRepository.findByIdForCurrentUser(id).flatMap(scorecardMapper::toOptionalDto);
  }

  /** Delete a scorecard by ID for the current authenticated user. */
  @Override
  public int deleteById(UUID id) {
    return scorecardRepository.deleteByIdForCurrentUser(id);
  }
}
