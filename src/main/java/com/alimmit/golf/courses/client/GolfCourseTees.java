package com.alimmit.golf.courses.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GolfCourseTees(List<GolfCourseTee> female, List<GolfCourseTee> male) {}
