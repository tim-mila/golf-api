package com.alimmit.golf.handicap;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.history.RevisionMetadata;

public record HandicapRevisionDto(
    UUID handicapId,
    String golferId,
    Instant createdAt,
    Double handicapIndex,
    Integer roundsUsed,
    Integer totalRounds,
    Integer revision,
    RevisionMetadata.RevisionType revisionType,
    Instant revisionTimestamp) {}
