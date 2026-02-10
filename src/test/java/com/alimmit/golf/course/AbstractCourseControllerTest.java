package com.alimmit.golf.course;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

class AbstractCourseControllerTest extends AbstractControllerMockMvc {

  protected AbstractCourseControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions listCourses(Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(get(CourseConstants.COURSE_ENDPOINT).with(jwt().jwt(fn::apply)), expect);
  }

  ResultActions getCourse(
      UUID courseId, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        get(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions createCourse(
      String requestBody, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        post(CourseConstants.COURSE_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions patchCourse(
      UUID courseId,
      String requestBody,
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        patch(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions deleteCourse(
      UUID courseId, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        delete(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }
}
