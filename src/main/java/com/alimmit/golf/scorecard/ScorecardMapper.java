package com.alimmit.golf.scorecard;

/**
 * Maps between ScorecardEntity (persistence layer) and ScorecardDto (API
 * layer).
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
        entity.getCourseName(),
        entity.getScore(),
        entity.getRating(),
        entity.getSlope());
  }

  /**
   * Convert request DTO to entity for persistence.
   * Audit fields will be populated automatically by JPA.
   * Course rating and slope rating are resolved by the service layer.
   */
  static ScorecardEntity toEntity(String scorecardId, ScorecardRequestDto request) {
    return new ScorecardEntity(
        scorecardId,
        request.scoreDate(),
        request.courseName(),
        request.score(),
        request.rating(),
        request.slope());
  }
}
