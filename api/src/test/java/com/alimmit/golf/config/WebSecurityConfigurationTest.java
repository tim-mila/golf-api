package com.alimmit.golf.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class WebSecurityConfigurationTest {

  private static final String ALLOWED_ORIGIN = "http://localhost:3000";
  private static final String DISALLOWED_ORIGIN = "https://evil.com";

  private CorsConfiguration corsConfig() {
    WebSecurityConfiguration config = new WebSecurityConfiguration();
    ReflectionTestUtils.setField(config, "allowedOrigins", List.of(ALLOWED_ORIGIN));
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
        .containsExactlyInAnyOrder("GET", "POST", "PATCH", "DELETE", "OPTIONS");
  }

  @Test
  void allowedHeaders_ContainExpected() {
    assertThat(corsConfig().getAllowedHeaders())
        .containsExactlyInAnyOrder("Authorization", "Content-Type");
  }
}
