package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ScorecardDto(
    UUID scorecardId,
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
