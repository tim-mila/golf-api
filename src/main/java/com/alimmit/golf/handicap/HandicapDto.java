package com.alimmit.golf.handicap;

import java.time.Instant;

public record HandicapDto(
    String handicapId,
    String golferId,
    Instant createdAt,
    Double handicapIndex,
    Integer roundsUsed,
    Integer totalRounds) {}
