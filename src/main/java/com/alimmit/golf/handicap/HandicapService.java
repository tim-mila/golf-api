package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;

import java.util.List;
import java.util.Optional;

public interface HandicapService {

  void calculate(List<ScorecardDto> scorecards, String golferId);

  /**
   * Get handicap for currently authenticated user
   *
   * @return Optional, most recent handicap
   */
  Optional<HandicapDto> getHandicap();

  /**
   * Get handicap history for currently authenticated user
   *
   * @return List of handicap indexes
   */
  List<HandicapRevisionDto> getHistory();
}
