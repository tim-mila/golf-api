package com.alimmit.golf.handicap;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.history.RevisionMetadata;

@Schema(description = "Represents a single revision in the handicap index history")
public record HandicapRevisionDto(
    @Schema(
            description = "Unique handicap record identifier",
            example = "01952a3b-f4c2-7000-8000-000000000004")
        UUID handicapId,
    @Schema(
            description = "Identifier of the golfer this handicap belongs to",
            example = "auth0|123")
        String golferId,
    @Schema(description = "Timestamp when the handicap record was first created") Instant createdAt,
    @Schema(description = "Timestamp when this revision was recorded") Instant lastModifiedAt,
    @Schema(description = "Handicap index at this revision", example = "14.7") Double handicapIndex,
    @Schema(
            description = "Number of differentials used to calculate the index at this revision",
            example = "8")
        Integer roundsUsed,
    @Schema(description = "Total number of rounds recorded at this revision", example = "12")
        Integer totalRounds,
    @Schema(description = "Revision number", example = "3") Integer revision,
    @Schema(description = "Type of change that created this revision (INSERT, UPDATE, DELETE)")
        RevisionMetadata.RevisionType revisionType,
    @Schema(description = "Timestamp when this revision was persisted")
        Instant revisionTimestamp) {}
