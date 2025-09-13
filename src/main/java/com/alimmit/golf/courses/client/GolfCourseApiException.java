package com.alimmit.golf.courses.client;

public class GolfCourseApiException extends RuntimeException {

  public GolfCourseApiException() {}

  public GolfCourseApiException(Exception e) {
    super(e);
  }

  public GolfCourseApiException(int statusCode) {
    super("GolfCourseApi status code exception | " + statusCode);
  }
}
