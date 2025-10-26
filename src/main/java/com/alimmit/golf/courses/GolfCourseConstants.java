package com.alimmit.golf.courses;

import com.alimmit.golf.GlobalConstants;

final class GolfCourseConstants {

  private GolfCourseConstants() {}

  static final String COURSES_ENDPOINT = GlobalConstants.API_V1_PREFIX + "/courses";
  static final String COURSES_SEARCH_ENDPOINT = COURSES_ENDPOINT + "/search";
}
