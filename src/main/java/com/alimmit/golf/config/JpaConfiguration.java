package com.alimmit.golf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

/**
 * JPA configuration for enabling audit support and Spring Security integration.
 * Works in conjunction with SpringSecurityAuditorAware to automatically
 * populate @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate fields.
 *
 * SecurityEvaluationContextExtension enables SpEL security expressions in
 * repository query methods (e.g., ?#{authentication.name}).
 */
@Configuration
@EnableJpaAuditing
class JpaConfiguration {

  @Bean
  SecurityEvaluationContextExtension securityEvaluationContextExtension() {
    return new SecurityEvaluationContextExtension();
  }
}
