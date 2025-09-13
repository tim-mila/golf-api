package com.alimmit.golf.courses.client;

import java.util.List;

/**
 * API client for golfcourseapi.com
 * 
 * {@see https://api.golfcourseapi.com/docs/api/}
 */
public interface GolfCourseApiClient {

  /**
   * Search for golf courses
   * 
   * @param terms Search terms
   * @return List of golf courses
   */
  List<GolfCourse> search(String terms);

  /**
   * Fetch a golf course
   * 
   * @param id Golf course identifier
   * @return Golf course
   */
  GolfCourse fetch(Long id);
}
