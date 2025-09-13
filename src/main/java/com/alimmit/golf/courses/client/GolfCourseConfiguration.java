package com.alimmit.golf.courses.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({GolfCourseApiConfigurationProperties.class})
class GolfCourseConfiguration {

  @Bean
  GolfCourseApiClient golfCourseApiClient(GolfCourseApiConfigurationProperties properties) {
    return new DefaultGolfCourseApiClientImpl(properties.getUrl(), properties.getApiKey());
  }
}
