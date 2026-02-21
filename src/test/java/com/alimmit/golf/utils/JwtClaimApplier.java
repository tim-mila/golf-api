package com.alimmit.golf.utils;

import org.springframework.security.oauth2.jwt.Jwt;

@FunctionalInterface
public interface JwtClaimApplier {
  Jwt.Builder apply(Jwt.Builder builder);
}
