package com.alimmit.golf.scorecard;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record ScorecardRequestDto(
    @JsonProperty("scoreDate") @NotNull LocalDate scoreDate,
    @JsonProperty("courseId") @NotNull Long courseId,
    @JsonProperty("score") @NotNull @Min(1) Integer score) {
}
