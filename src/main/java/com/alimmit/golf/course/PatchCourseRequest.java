package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;

@Schema(description = "Request body for partially updating a golf course — all fields are optional")
public record PatchCourseRequest(
    @Schema(description = "Name of the golf club", example = "Pebble Beach Golf Links")
        Optional<String> club,
    @Schema(description = "Name of the course within the club", example = "Pebble Beach")
        Optional<String> course,
    @Schema(description = "City where the course is located", example = "Pebble Beach")
        Optional<String> city,
    @Schema(description = "US state where the course is located", example = "CA")
        Optional<USState> state) {}
