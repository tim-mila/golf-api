package com.alimmit.golf.differential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.alimmit.golf.handicap.HandicapDto;
import com.alimmit.golf.handicap.HandicapService;
import com.alimmit.golf.scorecard.ScorecardRequestDto;
import com.alimmit.golf.scorecard.ScorecardType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DifferentialCalculatorImplTest {

  @Mock private HandicapService handicapService;

  private DifferentialCalculator differentialCalculator;

  @BeforeEach
  void setUp() {
    differentialCalculator = new DifferentialCalculatorImpl(handicapService);
  }

  @Test
  void testNineHoleDifferentialWithoutHandicap() {

    // GIVEN: no existing handicap index established
    when(handicapService.getHandicap()).thenReturn(Optional.empty());

    Differential differential =
        differentialCalculator.calculateDifferential(
            new ScorecardRequestDto(
                LocalDate.now(),
                ScorecardType.NINE,
                "Test Course",
                "Test Tee",
                42,
                36,
                35.6,
                126.0));

    assertThat(differential)
        .isNotNull()
        .hasFieldOrPropertyWithValue("differential", 5.7)
        .hasFieldOrPropertyWithValue("indexEstablished", false);
  }

  @Test
  void testNineHoleDifferentialWithHandicap() {

    // GIVEN: existing handicap index of 14.1
    when(handicapService.getHandicap())
        .thenReturn(
            Optional.of(new HandicapDto("hdcp-123", "auth0|123", Instant.now(), 14.1, 8, 20)));

    // WHEN: score posted of 38 on a 35.6/126 rating and slope course
    Differential differential =
        differentialCalculator.calculateDifferential(
            new ScorecardRequestDto(
                LocalDate.now(),
                ScorecardType.NINE,
                "Test Course",
                "Test Tee",
                38,
                36,
                35.6,
                126.0));

    // THEN: the calculated differential is 10.6
    // expected differential = 14.1 * 0.52 + 1.2 = 8.5
    // actual nine hole differential = ((38 - 35.6) * 113) / 126.0 = 2.2
    // calculated differential = 8.5 + 2.2 = 10.7
    assertThat(differential)
        .isNotNull()
        .hasFieldOrPropertyWithValue("differential", 10.7)
        .hasFieldOrPropertyWithValue("indexEstablished", true);
  }

  @Test
  void testEighteenHoleDifferentialWithoutHandicap() {

    // GIVEN: no existing handicap index established
    when(handicapService.getHandicap()).thenReturn(Optional.empty());

    // WHEN: score posted of 87 on a 70.9/123 rating and slope course
    Differential differential =
        differentialCalculator.calculateDifferential(
            new ScorecardRequestDto(
                LocalDate.now(),
                ScorecardType.EIGHTEEN,
                "Test Course",
                "Test Tee",
                87,
                72,
                70.9,
                123.0));

    // THEN: the calculated differential is 14.8
    // actual nine hole differential = ((87 - 70.8) * 113) / 123.0 = 14.8
    assertThat(differential)
        .isNotNull()
        .hasFieldOrPropertyWithValue("differential", 14.8)
        .hasFieldOrPropertyWithValue("indexEstablished", false);
  }

  @Test
  void testEighteenHoleDifferentialWithHandicap() {

    // GIVEN: existing handicap index of 14.1
    when(handicapService.getHandicap())
        .thenReturn(
            Optional.of(new HandicapDto("hdcp-123", "auth0|123", Instant.now(), 10.2, 8, 20)));

    // WHEN: score posted of 87 on a 70.9/123 rating and slope course
    Differential differential =
        differentialCalculator.calculateDifferential(
            new ScorecardRequestDto(
                LocalDate.now(),
                ScorecardType.EIGHTEEN,
                "Test Course",
                "Test Tee",
                85,
                72,
                71.2,
                124.0));

    // THEN: the calculated differential is 12.5
    // actual nine hole differential = ((85 - 71.2) * 113) / 124.0 = 14.8
    assertThat(differential)
        .isNotNull()
        .hasFieldOrPropertyWithValue("differential", 12.6)
        .hasFieldOrPropertyWithValue("indexEstablished", true);
  }
}
