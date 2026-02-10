package com.alimmit.golf.course;

import java.util.Optional;

public record PatchCourseRequest(
    Optional<String> club,
    Optional<String> course,
    Optional<String> city,
    Optional<USState> state) {}
