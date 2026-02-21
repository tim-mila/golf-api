package com.alimmit.golf.course;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.utils.JwtClaimApplier;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

class AbstractCourseControllerTest extends AbstractControllerMockMvc {

  protected AbstractCourseControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions listCourses(JwtClaimApplier fn, ResultMatcher... expect) throws Exception {
    return performMockMvc(fn, get(CourseConstants.COURSE_ENDPOINT), expect);
  }

  ResultActions getCourse(UUID courseId, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        get(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId),
        expect);
  }

  ResultActions createCourse(String requestBody, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        post(CourseConstants.COURSE_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody),
        expect);
  }

  ResultActions patchCourse(
      UUID courseId, String requestBody, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        patch(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody),
        expect);
  }

  ResultActions deleteCourse(UUID courseId, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        delete(CourseConstants.COURSE_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, courseId),
        expect);
  }
}
