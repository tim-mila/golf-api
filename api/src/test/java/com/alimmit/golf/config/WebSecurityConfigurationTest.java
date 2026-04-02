package com.alimmit.golf.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class WebSecurityConfigurationTest {

  private static final String ALLOWED_ORIGIN = "http://localhost:3000";
  private static final String DISALLOWED_ORIGIN = "https://evil.com";
  private static final List<String> ALLOWED_METHODS =
      List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS");
  private static final List<String> ALLOWED_HEADERS = List.of("Authorization", "Content-Type");
  private static final long MAX_AGE = 3600L;

  private CorsConfiguration corsConfig() {
    CorsProperties props =
        new CorsProperties(List.of(ALLOWED_ORIGIN), ALLOWED_METHODS, ALLOWED_HEADERS, MAX_AGE);
    WebSecurityConfiguration config = new WebSecurityConfiguration(props);
    UrlBasedCorsConfigurationSource source =
        (UrlBasedCorsConfigurationSource) config.corsConfigurationSource();
    return source.getCorsConfigurations().get("/**");
  }

  @Test
  void allowedOrigin_IsPermitted() {
    assertThat(corsConfig().checkOrigin(ALLOWED_ORIGIN)).isEqualTo(ALLOWED_ORIGIN);
  }

  @Test
  void disallowedOrigin_IsRejected() {
    assertThat(corsConfig().checkOrigin(DISALLOWED_ORIGIN)).isNull();
  }

  @Test
  void allowedMethods_ContainExpected() {
    assertThat(corsConfig().getAllowedMethods())
        .containsExactlyInAnyOrderElementsOf(ALLOWED_METHODS);
  }

  @Test
  void allowedHeaders_ContainExpected() {
    assertThat(corsConfig().getAllowedHeaders())
        .containsExactlyInAnyOrderElementsOf(ALLOWED_HEADERS);
  }

  @Test
  void maxAge_IsConfigured() {
    assertThat(corsConfig().getMaxAge()).isEqualTo(MAX_AGE);
  }
}
