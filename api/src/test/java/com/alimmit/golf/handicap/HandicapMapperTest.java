package com.alimmit.golf.handicap;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HandicapMapperTest {

  private final HandicapMapper mapper = new HandicapMapper();

  @Test
  void fromCalculationToEntity() {

    HandicapEntity entity =
        mapper.toEntity(
            new HandicapCalculation(11.3, 3, 5, Collections.emptyList(), Instant.now()), "Test");

    Assertions.assertThat(entity)
        .hasFieldOrPropertyWithValue("golferId", "Test")
        .hasFieldOrPropertyWithValue("handicapIndex", 11.3)
        .hasFieldOrPropertyWithValue("roundsUsed", 3)
        .hasFieldOrPropertyWithValue("totalRounds", 5);
  }

  @Test
  void fromEntityToDto() {

    HandicapEntity entity = new HandicapEntity("Test", 7.5, 9, 10);
    Assertions.assertThat(mapper.toDto(entity))
        .hasFieldOrProperty("handicapId")
        .hasFieldOrPropertyWithValue("golferId", "Test")
        .hasFieldOrPropertyWithValue("handicapIndex", 7.5)
        .hasFieldOrPropertyWithValue("roundsUsed", 9)
        .hasFieldOrPropertyWithValue("totalRounds", 10);
  }

  @Test
  void fromOptionalEntityToDto() {

    Optional<HandicapEntity> entity = Optional.of(new HandicapEntity("Test", 7.5, 9, 10));
    Assertions.assertThat(mapper.toOptionalDto(entity))
        .isPresent()
        .get()
        .hasFieldOrProperty("handicapId")
        .hasFieldOrPropertyWithValue("golferId", "Test")
        .hasFieldOrPropertyWithValue("handicapIndex", 7.5)
        .hasFieldOrPropertyWithValue("roundsUsed", 9)
        .hasFieldOrPropertyWithValue("totalRounds", 10);
  }

  @Test
  void fromEmptyOptionalToDto() {
    Assertions.assertThat(mapper.toOptionalDto(Optional.empty())).isNotPresent();
  }
}
