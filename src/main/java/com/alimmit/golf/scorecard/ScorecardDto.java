package com.alimmit.golf.scorecard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Represents a recorded golf round")
public record ScorecardDto(
    @Schema(
            description = "Unique scorecard identifier",
            example = "01952a3b-f4c2-7000-8000-000000000003")
        UUID scorecardId,
    @Schema(description = "Timestamp when the scorecard was created") Instant createdAt,
    @Schema(description = "Identifier of the user who created the scorecard") String createdBy,
    @Schema(description = "Date the round was played", example = "2026-03-10") LocalDate scoreDate,
    @Schema(description = "Name of the golf course played", example = "Pebble Beach")
        String courseName,
    @Schema(description = "Name of the tees played from", example = "Blue") String teeName,
    @Schema(description = "Gross score for the round", example = "88") Integer score,
    @Schema(description = "Par for the course from the selected tees", example = "72") Integer par,
    @Schema(
            description = "USGA course rating — expected score for a scratch golfer",
            example = "71.2")
        Double courseRating,
    @Schema(
            description = "USGA slope rating (55–155)",
            example = "131.0",
            minimum = "55",
            maximum = "155")
        Double slopeRating,
    @Schema(description = "Number of holes played", example = "EIGHTEEN")
        ScorecardType scorecardType,
    @Schema(description = "Calculated score differential for handicap purposes", example = "14.7")
        Double differential,
    @Schema(
            description = "Whether enough rounds have been recorded to establish a handicap index",
            example = "true")
        Boolean indexEstablished) {}
