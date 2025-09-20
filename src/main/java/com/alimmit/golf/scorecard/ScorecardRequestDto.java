package com.alimmit.golf.scorecard;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

record ScorecardRequestDto(@NotNull LocalDate scoreDate, @NotNull Long courseId,
    @NotNull @Min(1) Integer score) {

}
