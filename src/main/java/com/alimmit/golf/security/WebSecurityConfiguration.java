package com.alimmit.golf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
class WebSecurityConfiguration {
    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/**")
            .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
            .oauth2ResourceServer(c -> c.jwt(Customizer.withDefaults()))
            .build();
    }
}
