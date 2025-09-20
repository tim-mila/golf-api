package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record ScorecardDto(UUID scorecardId, Instant createdAt, String createdBy, Instant lastModifiedAt,
    String lastModifiedBy, LocalDate scoreDate, Long courseId, Integer score) {

}
