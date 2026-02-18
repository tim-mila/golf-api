package com.alimmit.golf.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTeeRequest(
    @NotBlank String name,
    @NotNull Integer yardage,
    @NotNull BigDecimal slope,
    @NotNull BigDecimal rating) {}
