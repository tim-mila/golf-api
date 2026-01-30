package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;

public record ScorecardDto(
    String scorecardId,
    Instant createdAt,
    String createdBy,
    Instant lastModifiedAt,
    String lastModifiedBy,
    LocalDate scoreDate,
    String courseName,
    Integer score,
    Double courseRating,
    Double slopeRating,
    ScorecardType scorecardType) {
}
