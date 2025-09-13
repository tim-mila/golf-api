package com.alimmit.golf.courses.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseTee(@JsonProperty("tee_name") String name,
    @JsonProperty("course_rating") Double courseRating,
    @JsonProperty("slope_rating") Double slopeRating,
    @JsonProperty("bogey_rating") Double bogeyRating,
    @JsonProperty("total_yards") Integer totalYards,
    @JsonProperty("total_meters") Integer totalMeters,
    @JsonProperty("number_of_holes") Integer numberOfHoles,
    @JsonProperty("par_total") Integer parTotal,
    @JsonProperty("front_course_rating") Double frontCourseRating,
    @JsonProperty("front_slope_rating") Double frontSlopeRating,
    @JsonProperty("front_bogey_rating") Double frontBogeyRating,
    @JsonProperty("back_course_rating") Double backCourseRating,
    @JsonProperty("back_slope_rating") Double backSlopeRating,
    @JsonProperty("back_bogey_rating") Double backBogeyRating,
    @JsonProperty("holes") List<GolfCourseHole> holes) {
}
