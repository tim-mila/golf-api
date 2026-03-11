package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Optional;

@Schema(description = "Request body for partially updating a tee — all fields are optional")
public record PatchTeeRequest(
    @Schema(description = "Tee name (e.g. Blue, White, Red)", example = "White")
        Optional<String> name,
    @Schema(description = "Par for the course from this tee", example = "72") Optional<Integer> par,
    @Schema(
            description = "USGA slope rating (55–155)",
            example = "125.0",
            minimum = "55",
            maximum = "155")
        Optional<BigDecimal> slope,
    @Schema(
            description = "USGA course rating — expected score for a scratch golfer",
            example = "69.5")
        Optional<BigDecimal> rating) {}
