package com.alimmit.golf.handicap;

import org.springframework.data.history.RevisionMetadata;

import java.time.Instant;
import java.util.UUID;

public record HandicapRevisionDto(
    UUID handicapId,
    String golferId,
    Instant createdAt,
    Double handicapIndex,
    Integer roundsUsed,
    Integer totalRounds,
    Integer revision,
    RevisionMetadata.RevisionType revisionType,
    Instant revisionTimestamp
) {}
