package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for calculating golf handicap handicapIndex using the World Handicap System (WHS).
 * </p>
 * WHS Formula:
 * 1. Score Differential = (113 / Slope Rating) × (Score - Course Rating)
 * 2. Handicap Index = Average of the best N differentials × 0.96
 * </p>
 * Number of differentials used based on rounds available:
 * 3 rounds: lowest 1 - 2.0
 * 4 rounds: lowest 1 - 1.0
 * 5 rounds: lowest 1
 * 6 rounds: lowest 2 - 1.0
 * 7-8 rounds: lowest 2
 * 9-11 rounds: lowest 3
 * 12-14 rounds: lowest 4
 * 15-16 rounds: lowest 5
 * 17-18 rounds: lowest 6
 * 19 rounds: lowest 7
 * 20+ rounds: lowest 8
 */
interface HandicapCalculator {

  /**
   * Calculate the handicap handicapIndex for the current authenticated user.
   *
   * @return Optional of HandicapDto with calculated handicap handicapIndex and details
   */
  Optional<HandicapCalculation> calculate(List<ScorecardDto> scorecards);
}
