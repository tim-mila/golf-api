package com.alimmit.golf.scorecard;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScorecardRequestDto(
    @JsonProperty("scoreDate") @NotNull LocalDate scoreDate,
    @JsonProperty("score") @NotNull @Min(1) Integer score,
    @JsonProperty("courseName") @NotBlank String courseName,
    @JsonProperty("teeName") @NotBlank String teeName,
    @JsonProperty("rating") @NotNull Double rating,
    @JsonProperty("slope") @NotNull Double slope) {
}
