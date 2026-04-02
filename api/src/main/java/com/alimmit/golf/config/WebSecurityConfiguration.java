package com.alimmit.golf.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
class WebSecurityConfiguration {

  private final CorsProperties corsProperties;

  WebSecurityConfiguration(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) {
    return http.securityMatcher("/**")
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            requests -> {
              requests.requestMatchers("/v1/api-docs/**").permitAll();
              requests.anyRequest().authenticated();
            })
        .oauth2ResourceServer(c -> c.jwt(Customizer.withDefaults()))
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(corsProperties.allowedOrigins());
    config.setAllowedMethods(corsProperties.allowedMethods());
    config.setAllowedHeaders(corsProperties.allowedHeaders());
    config.setMaxAge(corsProperties.maxAge());
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
