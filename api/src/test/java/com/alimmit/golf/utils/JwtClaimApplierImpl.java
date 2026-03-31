package com.alimmit.golf.utils;

import org.springframework.security.oauth2.jwt.Jwt;

public class JwtClaimApplierImpl implements JwtClaimApplier {

  private final String name;
  private final String sub;
  private final String[] scopes;

  public JwtClaimApplierImpl(String name, String sub, String[] scopes) {
    this.name = name;
    this.sub = sub;
    this.scopes = scopes;
  }

  @Override
  public Jwt.Builder apply(Jwt.Builder builder) {
    return builder.claim("name", name).claim("sub", sub).claim("scope", String.join(" ", scopes));
  }
}
