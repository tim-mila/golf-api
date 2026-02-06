package com.alimmit.golf.scorecard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.alimmit.golf.differential.Differential;
import com.alimmit.golf.differential.DifferentialCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for scorecard operations.
 * Handles business logic and transaction management.
 */
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
   * Create a new scorecard.
   * Generates ID and persists to database with audit fields.
   * If the rating and slope are not provided, they are looked up from the golf course API.
   */
  @Override
  public ScorecardDto create(ScorecardRequestDto request) {
    Differential differential = differentialCalculator.calculateDifferential(request);
    ScorecardEntity saved = scorecardRepository.save(
        scorecardMapper.toEntity(request, differential.differential(), differential.indexEstablished()));
    return scorecardMapper.toDto(saved);
  }

  /**
   * List all scorecards for the current authenticated user.
   */
  @Transactional(readOnly = true)
  @Override
  public List<ScorecardDto> listAll() {
    return scorecardRepository.findAllForCurrentUser()
        .stream()
        .map(scorecardMapper::toDto)
        .toList();
  }

  /**
   * List all scorecards for provided user
   *
   * @param userId User id
   * @return List of ScorecardDto
   */
  @Transactional(readOnly = true)
  @Override
  public List<ScorecardDto> listAll(String userId) {
    return scorecardRepository.findAllCreatedBy(userId).stream().map(scorecardMapper::toDto).toList();
  }

  /**
   * Get a single scorecard by ID for the current authenticated user.
   */
  @Transactional(readOnly = true)
  @Override
  public Optional<ScorecardDto> getById(UUID id) {
    return scorecardRepository
            .findByIdForCurrentUser(id)
            .flatMap(scorecardMapper::toOptionalDto);
  }

  /**
   * Delete a scorecard by ID for the current authenticated user.
   */
  @Override
  public int deleteById(UUID id) {
    return scorecardRepository.deleteByIdForCurrentUser(id);
  }
}
