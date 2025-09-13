package com.alimmit.golf.courses;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alimmit.golf.courses.client.GolfCourse;
import com.alimmit.golf.courses.client.GolfCourseApiClient;

@RestController
@RequestMapping("/v1/courses")
class GolfCourseController {

  private final GolfCourseApiClient client;

  public GolfCourseController(GolfCourseApiClient client) {
    this.client = client;
  }

  @GetMapping(path = "/search", params = {"q"})
  List<GolfCourse> search(@RequestParam("q") String searchTerms) {
    return client.search(searchTerms);
  }

  @GetMapping(path = "/{id}")
  GolfCourse fetch(@PathVariable("id") Long id) {
    return client.fetch(id);
  }
}
