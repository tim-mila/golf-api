package com.alimmit.golf.handicap;

import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;

public record HandicapRevisionDto(
    String handicapId,
    String golferId,
    Instant createdAt,
    Double handicapIndex,
    Integer roundsUsed,
    Integer totalRounds,
    Integer revision,
    RevisionMetadata.RevisionType revisionType,
    Instant revisionTimestamp
) {}
