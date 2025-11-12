package com.alimmit.golf.scorecard;

/**
 * Maps between ScorecardEntity (persistence layer) and ScorecardDto (API layer).
 */
class ScorecardMapper {

  private ScorecardMapper() {
    // Utility class
  }

  /**
   * Convert entity to DTO for API responses.
   */
  static ScorecardDto toDto(ScorecardEntity entity) {
    return new ScorecardDto(
        entity.getScorecardId(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getLastModifiedAt(),
        entity.getLastModifiedBy(),
        entity.getScoreDate(),
        entity.getCourseId(),
        entity.getScore());
  }

  /**
   * Convert request DTO to entity for persistence.
   * Audit fields will be populated automatically by JPA.
   */
  static ScorecardEntity toEntity(String scorecardId, ScorecardRequestDto request) {
    return new ScorecardEntity(
        scorecardId,
        request.scoreDate(),
        request.courseId(),
        request.score());
  }
}
