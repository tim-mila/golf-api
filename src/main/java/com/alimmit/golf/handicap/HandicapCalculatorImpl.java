package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;
import com.alimmit.golf.scorecard.ScorecardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
class HandicapCalculatorImpl implements HandicapCalculator {

  private static final Logger logger = LoggerFactory.getLogger(HandicapCalculatorImpl.class);

  private static final double EXCELLENCE_MULTIPLIER = 0.96;
  private static final int MINIMUM_HOLES = 54;
  private static final int MAXIMUM_ROUNDS_CONSIDERED = 20;

  /**
   * Calculate the handicap handicapIndex for the current authenticated user.
   *
   * @return HandicapDto with calculated handicap handicapIndex and details
   */
  public Optional<HandicapCalculation> calculate(List<ScorecardDto> scorecards) {

    // Normalize to holes played rather than rounds to account for nine scores
    int holesPlayed = scorecards.stream().map(ScorecardDto::scorecardType).mapToInt(ScorecardType::getHolesPlayed).sum();
    logger.debug("calculate handicap | holes played = {}", holesPlayed);

    // Need minimum 54 holes played to calculate handicap
    if (holesPlayed < MINIMUM_HOLES) {
      logger.debug("calculate handicap | did not meet minimum hole threshold");
      return Optional.empty();
    }

    // Extract differentials for all scorecards, sorted by date (most recent first)
    List<Double> allDifferentials = scorecards.stream()
        .sorted(Comparator.comparing(ScorecardDto::scoreDate).reversed())
        .limit(MAXIMUM_ROUNDS_CONSIDERED)
        .map(ScorecardDto::differential)
        .toList();

    // Determine how many differentials to use and any adjustment
    int differentialsToUse = getDifferentialsToUse(holesPlayed);
    logger.debug("calculate handicap | based on {} holes played using {} differentials", holesPlayed, differentialsToUse);
    double adjustment = getAdjustment(holesPlayed);
    logger.debug("calculate handicap | based on {} holes played using {} adjustment", holesPlayed, adjustment);

    // Get the best (lowest) differentials
    List<Double> bestDifferentials = allDifferentials.stream()
        .sorted()
        .limit(differentialsToUse)
        .toList();
    logger.debug("calculate handicap | best differentials {}", bestDifferentials);


    // Calculate average and apply multiplier
    double average = bestDifferentials.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);
    logger.debug("calculate handicap | average differential {}", average);

    double handicapIndex = (average + adjustment) * EXCELLENCE_MULTIPLIER;
    logger.debug("calculate handicap | handicap index after applying adjust and excellence multiplier {}", average);

    // Round to one decimal place
    handicapIndex = Math.round(handicapIndex * 10.0) / 10.0;

    return Optional.of(new HandicapCalculation(
        handicapIndex,
        differentialsToUse,
        scorecards.size(),
        bestDifferentials,
        Instant.now()));
  }

  /**
   * Determine how many differentials to use based on total holes played.
   */
  private int getDifferentialsToUse(int holesPlayed) {
    if (holesPlayed >= 360) return 8;
    if (holesPlayed == 342) return 7;
    if (holesPlayed >= 306) return 6;
    if (holesPlayed >= 270) return 5;
    if (holesPlayed >= 216) return 4;
    if (holesPlayed >= 162) return 3;
    if (holesPlayed >= 126) return 2;
    if (holesPlayed >= 90) return 1;
    if (holesPlayed >= 54) return 1;
    return 0;
  }

  /**
   * Get adjustment value based on holes played (applied before multiplier).
   */
  private double getAdjustment(int holesPlayed) {
    if (holesPlayed >= 54 && holesPlayed < 72) return -2.0;
    if (holesPlayed >= 72 && holesPlayed < 108) return -1.0;
    if (holesPlayed == 108) return -1.0;
    return 0.0;
  }
}
