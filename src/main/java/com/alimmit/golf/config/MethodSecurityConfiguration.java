package com.alimmit.golf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfiguration {

  @Bean
  AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
    return new AnnotationTemplateExpressionDefaults();
  }
}
