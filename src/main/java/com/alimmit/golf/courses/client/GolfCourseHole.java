package com.alimmit.golf.courses.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseHole(Integer par, Integer yardage, Integer handicap) {
}
