package com.alimmit.golf.scorecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ScorecardRequestDto(
    @JsonProperty("scoreDate") @NotNull LocalDate scoreDate,
    @JsonProperty("scorecardType") @NotNull ScorecardType scorecardType,
    @JsonProperty("score") @NotNull @Min(1) Integer score,
    @JsonProperty("courseName") @NotBlank String courseName,
    @JsonProperty("teeName") @NotBlank String teeName,
    @JsonProperty("rating") @NotNull Double rating,
    @JsonProperty("slope") @NotNull Double slope) {
}
