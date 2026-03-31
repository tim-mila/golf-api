package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for creating a new golf course")
public record CreateCourseRequest(
    @NotBlank @Schema(description = "Name of the golf club", example = "Pebble Beach Golf Links")
        String club,
    @NotBlank @Schema(description = "Name of the course within the club", example = "Pebble Beach")
        String course,
    @NotBlank @Schema(description = "City where the course is located", example = "Pebble Beach")
        String city,
    @NotNull @Schema(description = "US state where the course is located", example = "CA")
        USState state) {}
