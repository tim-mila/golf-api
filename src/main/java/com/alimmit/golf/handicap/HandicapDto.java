package com.alimmit.golf.handicap;

import java.time.Instant;
import java.util.UUID;

public record HandicapDto(
    UUID handicapId,
    String golferId,
    Instant createdAt,
    Instant lastModifiedAt,
    Double handicapIndex,
    Integer roundsUsed,
    Integer totalRounds) {}
