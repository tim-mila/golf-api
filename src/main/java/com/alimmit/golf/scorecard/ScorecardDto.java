package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;

public record ScorecardDto(
    String scorecardId,
    Instant createdAt,
    String createdBy,
    LocalDate scoreDate,
    String courseName,
    String teeName,
    Integer score,
    Integer par,
    Double courseRating,
    Double slopeRating,
    ScorecardType scorecardType,
    Double differential,
    Boolean indexEstablished) {}
