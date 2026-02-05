package com.alimmit.golf.handicap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

class HandicapMapperTest {

  private final HandicapMapper mapper = new HandicapMapper(new HandicapIdGenerator());

  @Test
  void fromCalculationToEntity() {

    HandicapEntity entity = mapper.toEntity(
        new HandicapCalculation(
            11.3,
            3,
            5,
            Collections.emptyList(),
            Instant.now()),
        "Test"
    );

    Assertions.assertThat(entity)
        .hasFieldOrPropertyWithValue("golferId", "Test")
        .hasFieldOrPropertyWithValue("handicapIndex", 11.3)
        .hasFieldOrPropertyWithValue("roundsUsed", 3)
        .hasFieldOrPropertyWithValue("totalRounds", 5);
  }

  @Test
  void fromEntityToDto() {

    HandicapEntity entity = new HandicapEntity(
        "hdcp-2345",
        "Test",
        7.5,
        9,
        10
    );
    Assertions.assertThat(mapper.toDto(entity))
        .hasFieldOrPropertyWithValue("handicapId", "hdcp-2345")
        .hasFieldOrPropertyWithValue("golferId", "Test")
        .hasFieldOrPropertyWithValue("handicapIndex", 7.5)
        .hasFieldOrPropertyWithValue("roundsUsed", 9)
        .hasFieldOrPropertyWithValue("totalRounds", 10);
  }

  @Test
  void fromOptionalEntityToDto() {

    Optional<HandicapEntity> entity = Optional.of(new HandicapEntity(
        "hdcp-2345",
        "Test",
        7.5,
        9,
        10
    ));
    Assertions.assertThat(mapper.toOptionalDto(entity))
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("handicapId", "hdcp-2345")
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