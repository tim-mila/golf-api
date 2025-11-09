package com.alimmit.golf.scorecard;

import org.springframework.stereotype.Component;

import com.alimmit.golf.id.IdGenerator;

@Component
class ScorecardIdGenerator extends IdGenerator {

  private static final String PREFIX = "scr-";

  @Override
  public String generate() {
    return PREFIX + generate(32);
  }
}
