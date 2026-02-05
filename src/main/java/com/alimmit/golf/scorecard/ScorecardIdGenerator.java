package com.alimmit.golf.scorecard;

import com.alimmit.golf.id.IdGenerator;
import org.springframework.stereotype.Component;

@Component
class ScorecardIdGenerator extends IdGenerator {

  private static final String PREFIX = "scr-";

  @Override
  public String generate() {
    return PREFIX + generate(32);
  }
}
