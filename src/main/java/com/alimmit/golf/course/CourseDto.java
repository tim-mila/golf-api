package com.alimmit.golf.course;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Represents a golf course")
public record CourseDto(
    @Schema(
            description = "Unique course identifier",
            example = "01952a3b-f4c2-7000-8000-000000000001")
        UUID courseId,
    @Schema(description = "Timestamp when the course was created") Instant createdAt,
    @Schema(description = "Timestamp when the course was last modified") Instant lastModifiedAt,
    @Schema(description = "Name of the golf club", example = "Pebble Beach Golf Links") String club,
    @Schema(description = "Name of the course within the club", example = "Pebble Beach")
        String course,
    @Schema(description = "City where the course is located", example = "Pebble Beach") String city,
    @Schema(description = "US state where the course is located", example = "CA") USState state) {}
