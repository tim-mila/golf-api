package com.alimmit.golf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.method.PrePostTemplateDefaults;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {

  @Bean
  PrePostTemplateDefaults prePostTemplateDefaults() {
    return new PrePostTemplateDefaults();
  }
}
