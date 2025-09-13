package com.alimmit.golf.courses.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseLocation(String address, String city, String state, String country,
    Double latitude, Double longitude) {
}
