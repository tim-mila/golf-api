package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Represents a set of tees for a golf course")
public record TeeDto(
    @Schema(description = "Unique tee identifier", example = "01952a3b-f4c2-7000-8000-000000000002")
        UUID teeId,
    @Schema(
            description = "Identifier of the course this tee belongs to",
            example = "01952a3b-f4c2-7000-8000-000000000001")
        UUID courseId,
    @Schema(description = "Timestamp when the tee was created") Instant createdAt,
    @Schema(description = "Timestamp when the tee was last modified") Instant lastModifiedAt,
    @Schema(description = "Tee name (e.g. Blue, White, Red)", example = "Blue") String name,
    @Schema(description = "Par for the course from this tee", example = "72") Integer par,
    @Schema(
            description = "USGA slope rating (55–155)",
            example = "131.0",
            minimum = "55",
            maximum = "155")
        BigDecimal slope,
    @Schema(
            description = "USGA course rating — expected score for a scratch golfer",
            example = "71.2")
        BigDecimal rating) {}
