package com.alimmit.golf.scorecard;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@ValidRating
public record ScorecardRequestDto(
    @JsonProperty("scoreDate") @NotNull LocalDate scoreDate,
    @JsonProperty("scorecardType") @NotNull ScorecardType scorecardType,
    @JsonProperty("courseName") @NotBlank String courseName,
    @JsonProperty("teeName") @NotBlank String teeName,
    @JsonProperty("score") @NotNull @Min(1) Integer score,
    @JsonProperty("par") @NotNull @Min(1) Integer par,
    @JsonProperty("rating") @NotNull Double rating,
    @JsonProperty("slope") @NotNull Double slope) {
}
