package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request body for adding a new tee to a golf course")
public record CreateTeeRequest(
    @NotBlank @Schema(description = "Tee name (e.g. Blue, White, Red)", example = "Blue")
        String name,
    @NotNull @Schema(description = "Par for the course from this tee", example = "72") Integer par,
    @NotNull
        @Schema(
            description = "USGA slope rating (55–155)",
            example = "131.0",
            minimum = "55",
            maximum = "155")
        BigDecimal slope,
    @NotNull
        @Schema(
            description = "USGA course rating — expected score for a scratch golfer",
            example = "71.2")
        BigDecimal rating) {}
