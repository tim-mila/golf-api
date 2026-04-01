package com.alimmit.golf.handicap;

import com.alimmit.golf.GlobalConstants;

final class HandicapConstants {

  private HandicapConstants() {}

  static final String HANDICAP_ENDPOINT = GlobalConstants.API_V1_PREFIX + "/handicap";

  /** Maximum number of recent rounds considered by the World Handicap System. */
  static final int HANDICAP_LOOKBACK_ROUNDS = 20;
}
