package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ScorecardIdGeneratorTest {

  private ScorecardIdGenerator generator = new ScorecardIdGenerator();

  @Test
  void testGenerate() {
    assertThat(generator.generate())
        .hasSize(36)
        .startsWith("scr-");
  }

  @Test
  void testUnique() {
    Set<String> set = new HashSet<>();
    for (int i = 0; i < 10000; i++) {
      set.add(generator.generate());
    }
    assertThat(set).hasSize(10000);
  }
}
