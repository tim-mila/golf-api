package com.alimmit.golf.courses.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) 
public record GolfCourse(Long id, @JsonProperty("club_name") String clubName,
    @JsonProperty("course_name") String courseName, GolfCourseLocation location,
    GolfCourseTees tees) {
}
