package com.alimmit.golf.course;

import java.time.Instant;
import java.util.UUID;

public record CourseDto(
    UUID courseId,
    Instant createdAt,
    Instant lastModifiedAt,
    String club,
    String course,
    String city,
    USState state) {}
