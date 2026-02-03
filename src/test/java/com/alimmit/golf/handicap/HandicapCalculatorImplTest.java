package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardDto;
import com.alimmit.golf.scorecard.ScorecardType;
import com.alimmit.golf.utils.JwtPersona;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

class HandicapCalculatorImplTest {

  private final HandicapCalculator handicapCalculator = new HandicapCalculatorImpl();

  @Test
  void assertEmptyHandicap() {

    ScorecardDto scorecard1 = createScorecard(3, 99, 72.1, 123.3, 23.8);
    ScorecardDto scorecard2 = createScorecard(2, 98, 72.2, 123.2, 23.7);

    // Any input less than two should not return a calculated handicap handicapIndex
    Assertions.assertThat(handicapCalculator.calculate(Collections.emptyList())).isEmpty();
    Assertions.assertThat(handicapCalculator.calculate(List.of(scorecard1))).isEmpty();
    Assertions.assertThat(handicapCalculator.calculate(List.of(scorecard1, scorecard2))).isEmpty();
  }

  @Test
  void assertHandicapPresent() {

    ScorecardDto scorecard1 = createScorecard(3, 99, 72.1, 123.3, 23.9);
    ScorecardDto scorecard2 = createScorecard(2, 98, 72.2, 123.2, 23.8);
    ScorecardDto scorecard3 = createScorecard(1, 97, 72.3, 123.1, 23.7);

    // Any input less than two should not return a calculated handicap handicapIndex
    Assertions.assertThat(handicapCalculator.calculate(List.of(scorecard1, scorecard2, scorecard3)))
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("roundsUsed", 1)
        .hasFieldOrPropertyWithValue("totalRounds", 3)
        .hasFieldOrProperty("differentials")
    ;
  }

  private ScorecardDto createScorecard(int offsetDays, int score, double rating, double slope, double differential) {
    String id = "scr-" + RandomStringUtils.insecure().nextAlphanumeric(32);
    return new ScorecardDto(
        id,
        Instant.now().minus(offsetDays, ChronoUnit.DAYS),
        JwtPersona.GARY_GOLFER.sub(),
        LocalDate.now().minusDays(offsetDays),
        "Test Course",
        "Test Tee",
        score,
        rating,
        slope,
        ScorecardType.EIGHTEEN,
        differential,
        true);
  }
}