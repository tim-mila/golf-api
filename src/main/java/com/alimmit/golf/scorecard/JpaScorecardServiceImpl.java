package com.alimmit.golf.scorecard;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alimmit.golf.errors.NotFoundException;

/**
 * Service layer for scorecard operations.
 * Handles business logic and transaction management.
 */
@Service
@Transactional
class JpaScorecardServiceImpl implements ScorecardService {

  private final ScorecardRepository scorecardRepository;
  private final ScorecardIdGenerator scorecardIdGenerator;

  JpaScorecardServiceImpl(
          ScorecardRepository scorecardRepository,
          ScorecardIdGenerator scorecardIdGenerator) {
    this.scorecardRepository = scorecardRepository;
    this.scorecardIdGenerator = scorecardIdGenerator;
  }

  /**
   * Create a new scorecard.
   * Generates ID and persists to database with audit fields.
   * If the rating and slope are not provided, they are looked up from the golf course API.
   */
  @Override
  public ScorecardDto create(ScorecardRequestDto request) {
    String scorecardId = scorecardIdGenerator.generate();
    ScorecardEntity entity = ScorecardMapper.toEntity(scorecardId, request);
    ScorecardEntity saved = scorecardRepository.save(entity);
    return ScorecardMapper.toDto(saved);
  }

  /**
   * List all scorecards for the current authenticated user.
   */
  @Transactional(readOnly = true)
  @Override
  public List<ScorecardDto> listAll() {
    return scorecardRepository.findAllForCurrentUser()
        .stream()
        .map(ScorecardMapper::toDto)
        .toList();
  }

  /**
   * Get a single scorecard by ID for the current authenticated user.
   *
   * @throws NotFoundException if scorecard not found or not owned by current user
   */
  @Transactional(readOnly = true)
  @Override
  public ScorecardDto getById(String id) {
    return scorecardRepository.findByIdForCurrentUser(id)
        .map(ScorecardMapper::toDto)
        .orElseThrow(NotFoundException::new);
  }

  /**
   * Delete a scorecard by ID for the current authenticated user.
   *
   * @throws NotFoundException if scorecard not found or not owned by current user
   */
  @Override
  public void deleteById(String id) {
    int deletedCount = scorecardRepository.deleteByIdForCurrentUser(id);
    if (deletedCount == 0) {
      throw new NotFoundException();
    }
  }
}
