package com.alimmit.golf.handicap;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for handicap handicapIndex calculation.
 *
 * @param handicapIndex The calculated handicap handicapIndex (null if insufficient rounds)
 * @param roundsUsed Number of score differentials used in calculation
 * @param totalRounds Total number of rounds available
 * @param differentials List of score differentials used (sorted best to worst)
 * @param calculatedAt Timestamp of calculation
 */
public record HandicapCalculation(
    Double handicapIndex,
    int roundsUsed,
    int totalRounds,
    List<Double> differentials,
    Instant calculatedAt) {}
