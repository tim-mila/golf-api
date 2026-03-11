package com.alimmit.golf.handicap;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Represents a golfer's current handicap index")
public record HandicapDto(
    @Schema(
            description = "Unique handicap record identifier",
            example = "01952a3b-f4c2-7000-8000-000000000004")
        UUID handicapId,
    @Schema(
            description = "Identifier of the golfer this handicap belongs to",
            example = "auth0|123")
        String golferId,
    @Schema(description = "Timestamp when the handicap record was first created") Instant createdAt,
    @Schema(description = "Timestamp when the handicap index was last recalculated")
        Instant lastModifiedAt,
    @Schema(description = "Current handicap index", example = "14.7") Double handicapIndex,
    @Schema(
            description = "Number of differentials used to calculate the current index",
            example = "8")
        Integer roundsUsed,
    @Schema(description = "Total number of rounds recorded", example = "12") Integer totalRounds) {}
