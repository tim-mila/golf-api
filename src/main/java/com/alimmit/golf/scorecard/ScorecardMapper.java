package com.alimmit.golf.scorecard;

import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Maps between ScorecardEntity (persistence layer) and ScorecardDto (API
 * layer).
 */
@Component
class ScorecardMapper {

  /**
   * Convert entity to DTO for API responses.
   */
  ScorecardDto toDto(ScorecardEntity entity) {
    return new ScorecardDto(
        entity.getScorecardId(),
        entity.getCreatedAt(),
        entity.getCreatedBy(),
        entity.getScoreDate(),
        entity.getCourseName(),
        entity.getTeeName(),
        entity.getScore(),
        entity.getPar(),
        entity.getRating(),
        entity.getSlope(),
        entity.getScorecardType(),
        entity.getDifferential(),
        entity.isIndexEstablished());
  }

  Optional<ScorecardDto> toOptionalDto(ScorecardEntity entity) {
    return Optional.of(toDto(entity));
  }

  /**
   * Convert request DTO to entity for persistence.
   * Audit fields will be populated automatically by JPA.
   * Course rating and slope rating are resolved by the service layer.
   */
  ScorecardEntity toEntity(ScorecardRequestDto request, double differential, boolean indexEstablished1) {
    return new ScorecardEntity(
        request.scoreDate(),
        request.courseName(),
        request.teeName(),
        request.score(),
        request.par(),
        request.rating(),
        request.slope(),
        request.scorecardType(),
        differential,
        indexEstablished1);
  }
}
