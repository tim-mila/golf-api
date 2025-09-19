package com.alimmit.golf.courses.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "golf.course-api")
class GolfCourseApiConfigurationProperties {

  private final String url;
  private final String apiKey;

  public GolfCourseApiConfigurationProperties(String url, String apiKey) {
    this.url = url;
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getUrl() {
    return url;
  }
}
