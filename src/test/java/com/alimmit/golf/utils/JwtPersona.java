package com.alimmit.golf.utils;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Build some standard personas to use in MockMvc tests
 */
public interface JwtPersona {

  String getName();

  String getSub();

  public static final JwtPersona GARY_GOLFER = new JwtPersonalImpl("Gary Golfer", "123");
  public static final JwtPersona PAT_PUTTER = new JwtPersonalImpl("Pat Putter", "234");
  public static final JwtPersona DANA_DRIVER = new JwtPersonalImpl("Dana Driver", "345");

  public static Jwt.Builder forGaryGolfer(Jwt.Builder builder) {
    return builder.claim("name", GARY_GOLFER.getName()).claim("sub", GARY_GOLFER.getSub());
  }

  public static Jwt.Builder forPatPutter(Jwt.Builder builder) {
    return builder.claim("name", PAT_PUTTER.getName()).claim("sub", PAT_PUTTER.getSub());
  }

  public static Jwt.Builder forDanaDriver(Jwt.Builder builder) {
    return builder.claim("name", DANA_DRIVER.getName()).claim("sub", DANA_DRIVER.getSub());
  }
}