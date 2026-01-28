package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
class HandicapCalculatorImpl implements HandicapCalculator {

  private static final double STANDARD_SLOPE = 113.0;
  private static final double EXCELLENCE_MULTIPLIER = 0.96;
  private static final int MINIMUM_ROUNDS = 3;
  private static final int MAXIMUM_ROUNDS_CONSIDERED = 20;

  /**
   * Calculate the handicap handicapIndex for the current authenticated user.
   *
   * @return HandicapDto with calculated handicap handicapIndex and details
   */
  public Optional<HandicapCalculation> calculate(List<ScorecardDto> scorecards) {

    // Calculate differentials for all scorecards, sorted by date (most recent first)
    List<Double> allDifferentials = scorecards.stream()
        .sorted(Comparator.comparing(ScorecardDto::scoreDate).reversed())
        .limit(MAXIMUM_ROUNDS_CONSIDERED)
        .map(this::calculateDifferential)
        .toList();

    int totalRounds = allDifferentials.size();

    // Need minimum 3 rounds to calculate handicap
    if (totalRounds < MINIMUM_ROUNDS) {
      return Optional.empty();
    }

    // Determine how many differentials to use and any adjustment
    int differentialsToUse = getDifferentialsToUse(totalRounds);
    double adjustment = getAdjustment(totalRounds);

    // Get the best (lowest) differentials
    List<Double> bestDifferentials = allDifferentials.stream()
        .sorted()
        .limit(differentialsToUse)
        .toList();

    // Calculate average and apply multiplier
    double average = bestDifferentials.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

    double handicapIndex = (average + adjustment) * EXCELLENCE_MULTIPLIER;

    // Round to one decimal place
    handicapIndex = Math.round(handicapIndex * 10.0) / 10.0;

    return Optional.of(new HandicapCalculation(
        handicapIndex,
        differentialsToUse,
        totalRounds,
        bestDifferentials,
        Instant.now()));
  }

  /**
   * Calculate score differential for a single round.
   * Formula: (113 / Slope Rating) × (Score - Course Rating)
   */
  double calculateDifferential(ScorecardDto scorecard) {
    double differential = (STANDARD_SLOPE / scorecard.slopeRating())
        * (scorecard.score() - scorecard.courseRating());
    // Round to one decimal place
    return Math.round(differential * 10.0) / 10.0;
  }

  /**
   * Determine how many differentials to use based on total rounds.
   */
  private int getDifferentialsToUse(int totalRounds) {
    if (totalRounds >= 20) return 8;
    if (totalRounds == 19) return 7;
    if (totalRounds >= 17) return 6;
    if (totalRounds >= 15) return 5;
    if (totalRounds >= 12) return 4;
    if (totalRounds >= 9) return 3;
    if (totalRounds >= 7) return 2;
    if (totalRounds >= 5) return 1;
    if (totalRounds >= 3) return 1;
    return 0;
  }

  /**
   * Get adjustment value based on total rounds (applied before multiplier).
   */
  private double getAdjustment(int totalRounds) {
    if (totalRounds == 3) return -2.0;
    if (totalRounds == 4) return -1.0;
    if (totalRounds == 6) return -1.0;
    return 0.0;
  }
}
