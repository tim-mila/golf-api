package com.alimmit.golf.scorecard;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CourseRatingValidatorImplTest {

  ConstraintValidator<ValidRating, ScorecardRequestDto> validator = new CourseRatingValidatorImpl();

  @Test
  void testRatingMaxAbovePar() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        72,
        77.0,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isTrue();
  }

  @Test
  void testRatingMaxBelowPar() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        72,
        67.0,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isTrue();
  }

  @Test
  void testRatingInvalidAbovePar() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        72,
        82.1,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isFalse();
  }

  @Test
  void testRatingInvalidBelowPar() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        72,
        61.9,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isFalse();
  }

  @Test
  void testRatingNullPar() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        null,
        77.1,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isFalse();
  }

  @Test
  void testRatingNullRating() {

    ScorecardRequestDto request = new ScorecardRequestDto(
        LocalDate.now(),
        ScorecardType.EIGHTEEN,
        "Test",
        "Test",
        88,
        72,
        null,
        125.0);

    assertThat(validator.isValid(request, mock(ConstraintValidatorContext.class))).isFalse();
  }
}