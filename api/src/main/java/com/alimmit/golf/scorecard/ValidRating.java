package com.alimmit.golf.scorecard;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** Validation annotation applied to course rating in scorecard requests */
@Documented
@Constraint(validatedBy = CourseRatingValidatorImpl.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface ValidRating {
  String message() default "Scorecard course rating ";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
