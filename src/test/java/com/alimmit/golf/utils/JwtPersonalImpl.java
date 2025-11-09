package com.alimmit.golf.utils;

class JwtPersonalImpl implements JwtPersona {
  private final String name;
  private final String sub;

  JwtPersonalImpl(String name, String sub) {
    this.name = name;
    this.sub = sub;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getSub() {
    return sub;
  }
}
