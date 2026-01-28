package com.alimmit.golf.handicap;

import java.util.Optional;

interface HandicapService {

  void calculate(String userId);

  /**
   * Get handicap for currently authenticated user
   *
   * @return Optional, most recent handicap
   */
  Optional<HandicapDto> getHandicap();
}
