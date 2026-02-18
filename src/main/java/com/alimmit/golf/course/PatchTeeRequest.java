package com.alimmit.golf.course;

import java.math.BigDecimal;
import java.util.Optional;

public record PatchTeeRequest(
    Optional<String> name,
    Optional<Integer> yardage,
    Optional<BigDecimal> slope,
    Optional<BigDecimal> rating) {}
