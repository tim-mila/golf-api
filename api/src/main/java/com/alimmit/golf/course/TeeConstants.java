package com.alimmit.golf.course;

import com.alimmit.golf.GlobalConstants;

final class TeeConstants {

  private TeeConstants() {}

  static final String TEE_ENDPOINT = GlobalConstants.API_V1_PREFIX + "/tee";

  static final String TEE_BY_ID_ENDPOINT =
      GlobalConstants.API_V1_PREFIX + "/tee" + GlobalConstants.API_RECORD_SUFFIX;
}
