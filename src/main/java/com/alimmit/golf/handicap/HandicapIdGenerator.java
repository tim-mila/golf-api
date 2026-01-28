package com.alimmit.golf.handicap;

import com.alimmit.golf.id.IdGenerator;
import org.springframework.stereotype.Component;

@Component
class HandicapIdGenerator extends IdGenerator {

  private static final String PREFIX = "hdcp-";

  @Override
  public String generate() {
    return PREFIX + generate(32);
  }
}
