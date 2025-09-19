package com.alimmit.golf.courses.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseFetchResponse(GolfCourse course) {
}
