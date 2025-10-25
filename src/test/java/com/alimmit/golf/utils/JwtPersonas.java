package com.alimmit.golf.utils;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Build some standard personas to use in MockMvc tests
 */
public interface JwtPersonas {

  public static Jwt.Builder forGaryGolfer(Jwt.Builder builder) {
    return builder.claim("name", "Gary Golfer").claim("sub", "123");
  }

  public static Jwt.Builder forPatPutter(Jwt.Builder builder) {
    return builder.claim("name", "Pat Putter").claim("sub", "234");
  }

  public static Jwt.Builder forDanaDriver(Jwt.Builder builder) {
    return builder.claim("name", "Dana Driver").claim("sub", "345");
  }
}