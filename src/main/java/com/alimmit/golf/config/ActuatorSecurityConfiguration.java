package com.alimmit.golf.config;

import com.alimmit.golf.GlobalConstants;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class ActuatorSecurityConfiguration {

  @Bean
  @Order(1)
  SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
    return http.securityMatcher(EndpointRequest.toAnyEndpoint())
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class))
                    .permitAll()
                    .anyRequest()
                    .hasAuthority(
                        "SCOPE_"
                            + GlobalConstants.SCOPE_PERMISSION_READ
                            + ":"
                            + GlobalConstants.SCOPE_ACTUATOR))
        .oauth2ResourceServer(c -> c.jwt(Customizer.withDefaults()))
        .build();
  }
}
