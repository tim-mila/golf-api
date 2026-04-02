package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;
import java.util.List;

public interface HandicapService {

  void calculate(List<ScorecardDto> scorecards, String golferId);

  /**
   * Get handicap for currently authenticated user
   *
   * @return HandicapDto with established=true and full data when threshold met, established=false
   *     with null fields otherwise
   */
  HandicapDto getHandicap();

  /**
   * Get handicap history for currently authenticated user
   *
   * @return List of handicap indexes
   */
  List<HandicapRevisionDto> getHistory();
}
