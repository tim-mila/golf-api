package com.alimmit.golf.utils;

import com.alimmit.golf.GlobalConstants;
import org.springframework.security.oauth2.jwt.Jwt;

/** Build some standard personas to use in MockMvc tests */
public interface JwtPersona {

  String name();

  String sub();

  JwtPersona GARY_GOLFER = new JwtPersonalImpl("Gary Golfer", "123");
  JwtPersona PAT_PUTTER = new JwtPersonalImpl("Pat Putter", "234");
  JwtPersona DANA_DRIVER = new JwtPersonalImpl("Dana Driver", "345");

  static Jwt.Builder forGaryGolfer(Jwt.Builder builder) {
      return withDefaultScopes(builder.claim("name", GARY_GOLFER.name()).claim("sub", GARY_GOLFER.sub()));
  }

  static Jwt.Builder forPatPutter(Jwt.Builder builder) {
    return withDefaultScopes(builder.claim("name", PAT_PUTTER.name()).claim("sub", PAT_PUTTER.sub()));
  }

  static Jwt.Builder forDanaDriver(Jwt.Builder builder) {
    return withDefaultScopes(builder.claim("name", DANA_DRIVER.name()).claim("sub", DANA_DRIVER.sub()));
  }

  static Jwt.Builder forGaryGolferReadOnly(Jwt.Builder builder) {
    return builder
        .claim("name", GARY_GOLFER.name())
        .claim("sub", GARY_GOLFER.sub())
        .claim("scope", scopeReadPermission(GlobalConstants.SCOPE_SCORECARD));
  }

  static Jwt.Builder forGaryGolferWriteOnly(Jwt.Builder builder) {
    return builder
        .claim("name", GARY_GOLFER.name())
        .claim("sub", GARY_GOLFER.sub())
        .claim("scope", scopeWritePermission(GlobalConstants.SCOPE_SCORECARD));
  }

  static Jwt.Builder forGaryGolferNoHandicapScope(Jwt.Builder builder) {
    return builder
        .claim("name", GARY_GOLFER.name())
        .claim("sub", GARY_GOLFER.sub())
        .claim("scope", allPermissions(GlobalConstants.SCOPE_SCORECARD));
  }

  private static Jwt.Builder withDefaultScopes(Jwt.Builder builder) {
    return builder.claim(
        "scope",
        String.join(
            " ",
            allPermissions(GlobalConstants.SCOPE_SCORECARD),
            scopeReadPermission(GlobalConstants.SCOPE_HANDICAP)));
  }

  private static String allPermissions(String scope) {
    return String.join(" ", scopeReadPermission(scope), scopeWritePermission(scope));
  }

  private static String scopeReadPermission(String scope) {
    return GlobalConstants.SCOPE_PERMISSION_READ + ":" + scope;
  }

  private static String scopeWritePermission(String scope) {
    return GlobalConstants.SCOPE_PERMISSION_WRITE + ":" + scope;
  }
}
