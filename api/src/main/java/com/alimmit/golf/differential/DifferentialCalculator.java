package com.alimmit.golf.differential;

import com.alimmit.golf.scorecard.ScorecardRequestDto;

public interface DifferentialCalculator {

  /**
   * Calculate differential for a given scorecard
   *
   * @param scorecard Scorecard information
   * @return differential calculation result
   */
  Differential calculateDifferential(ScorecardRequestDto scorecard);
}
