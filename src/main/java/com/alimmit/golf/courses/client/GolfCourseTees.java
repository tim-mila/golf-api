package com.alimmit.golf.courses.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseTees(List<GolfCourseTee> female, List<GolfCourseTee> male) {
}
