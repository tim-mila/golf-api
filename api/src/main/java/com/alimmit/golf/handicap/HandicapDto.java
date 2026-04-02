package com.alimmit.golf.handicap;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Represents a golfer's current handicap index")
public record HandicapDto(
    @Schema(
            description =
                "Whether the golfer has met the minimum 54-hole threshold to establish a handicap index")
        boolean established,
    @Schema(
            description = "Unique handicap record identifier. Null when established is false.",
            example = "01952a3b-f4c2-7000-8000-000000000004")
        UUID handicapId,
    @Schema(
            description =
                "Identifier of the golfer this handicap belongs to. Null when established is false.",
            example = "auth0|123")
        String golferId,
    @Schema(
            description =
                "Timestamp when the handicap record was first created. Null when established is false.")
        Instant createdAt,
    @Schema(
            description =
                "Timestamp when the handicap index was last recalculated. Null when established is false.")
        Instant lastModifiedAt,
    @Schema(
            description = "Current handicap index. Null when established is false.",
            example = "14.7")
        Double handicapIndex,
    @Schema(
            description =
                "Number of differentials used to calculate the current index. Null when established is false.",
            example = "8")
        Integer roundsUsed,
    @Schema(
            description = "Total number of rounds recorded. Null when established is false.",
            example = "12")
        Integer totalRounds) {

  public static HandicapDto unestablished() {
    return new HandicapDto(false, null, null, null, null, null, null, null);
  }
}
