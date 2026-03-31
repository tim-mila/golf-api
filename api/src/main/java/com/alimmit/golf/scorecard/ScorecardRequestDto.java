package com.alimmit.golf.scorecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Request body for recording a round of golf")
@ValidRating
public record ScorecardRequestDto(
    @JsonProperty("scoreDate")
        @NotNull
        @Schema(description = "Date the round was played", example = "2026-03-10")
        LocalDate scoreDate,
    @JsonProperty("scorecardType")
        @NotNull
        @Schema(description = "Number of holes played", example = "EIGHTEEN")
        ScorecardType scorecardType,
    @JsonProperty("courseName")
        @NotBlank
        @Schema(description = "Name of the golf course played", example = "Pebble Beach")
        String courseName,
    @JsonProperty("teeName")
        @NotBlank
        @Schema(description = "Name of the tees played from", example = "Blue")
        String teeName,
    @JsonProperty("score")
        @NotNull
        @Min(1)
        @Schema(description = "Gross score for the round", example = "88")
        Integer score,
    @JsonProperty("par")
        @NotNull
        @Min(1)
        @Schema(description = "Par for the course from the selected tees", example = "72")
        Integer par,
    @JsonProperty("rating")
        @NotNull
        @Schema(
            description = "USGA course rating — expected score for a scratch golfer",
            example = "71.2")
        Double rating,
    @JsonProperty("slope")
        @NotNull
        @Schema(
            description = "USGA slope rating (55–155)",
            example = "131.0",
            minimum = "55",
            maximum = "155")
        Double slope) {}
