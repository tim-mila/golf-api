package com.alimmit.golf.scorecard;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Course rating is relative to the course par, typically par +/- 5
 */
class CourseRatingValidatorImpl implements ConstraintValidator<ValidRating, ScorecardRequestDto> {

  @Override
  public boolean isValid(ScorecardRequestDto scorecardRequestDto, ConstraintValidatorContext constraintValidatorContext) {
    Double rating = scorecardRequestDto.rating();
    Integer par = scorecardRequestDto.par();
    return (rating != null && par != null) && (rating <= par + 5) && (rating >= par - 5);
  }
}
