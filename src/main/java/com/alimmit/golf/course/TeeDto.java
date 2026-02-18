package com.alimmit.golf.course;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TeeDto(
    UUID teeId,
    UUID courseId,
    Instant createdAt,
    Instant lastModifiedAt,
    String name,
    Integer yardage,
    BigDecimal slope,
    BigDecimal rating) {}
