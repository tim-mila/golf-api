package com.alimmit.golf.handicap;

import com.alimmit.golf.GlobalConstants;

final class HandicapConstants {

  private HandicapConstants() {}

  static final String HANDICAP_ENDPOINT = GlobalConstants.API_V1_PREFIX + "/handicap";

  /** Maximum number of recent rounds considered by the World Handicap System. */
  static final int HANDICAP_LOOKBACK_ROUNDS = 20;

  /** Minimum holes played required to establish a handicap index. */
  static final int MINIMUM_HOLES_FOR_INDEX = 54;

  /** WHS excellence multiplier applied to the average differential. */
  static final double EXCELLENCE_MULTIPLIER = 0.96;
}
