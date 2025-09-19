package com.alimmit.golf.courses;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alimmit.golf.courses.client.GolfCourse;
import com.alimmit.golf.courses.client.GolfCourseApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1/courses")
@Tag(name = "Golf Courses")
class GolfCourseController {

  private final GolfCourseApiClient client;

  public GolfCourseController(GolfCourseApiClient client) {
    this.client = client;
  }

  @Operation(method = "GET", operationId = "courses.search", summary = "Search for golf courses",
      description = "Search for golf courses by name",
      parameters = {
          @Parameter(name = "q", in = ParameterIn.QUERY, description = "Golf course search terms")})

  @GetMapping(path = "/search", params = {"q"})
  List<GolfCourse> search(@RequestParam("q") String searchTerms) {
    return client.search(searchTerms);
  }

  @Operation(method = "GET", operationId = "courses.get",
      summary = "Fetch a golf course by identifier",
      description = "Fetch a single course by it's unique identifier",
      parameters = {
          @Parameter(name = "id", in = ParameterIn.PATH, description = "Golf course identifier")})
  @GetMapping(path = "/{id}")
  GolfCourse fetch(@PathVariable("id") Long id) {
    return client.fetch(id);
  }
}
