package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;

record ScorecardDto(
    String scorecardId,
    Instant createdAt,
    String createdBy,
    Instant lastModifiedAt,
    String lastModifiedBy,
    LocalDate scoreDate,
    Long courseId,
    Integer score) {
}
