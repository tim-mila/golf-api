package com.alimmit.golf.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provides the current auditor (user) from Spring Security context
 * for JPA audit fields (@CreatedBy, @LastModifiedBy).
 */
@Component
class SpringSecurityAuditorAware implements AuditorAware<String> {

  @NonNull
  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    return Optional.of(authentication.getName());
  }
}
