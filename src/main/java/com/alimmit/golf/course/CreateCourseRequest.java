package com.alimmit.golf.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCourseRequest(
    @NotBlank String club,
    @NotBlank String course,
    @NotBlank String city,
    @NotNull USState state) {}
