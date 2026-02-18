package com.alimmit.golf.course;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.alimmit.golf.AbstractControllerMockMvc;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

class AbstractTeeControllerTest extends AbstractControllerMockMvc {

  protected AbstractTeeControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions listTeesByCourse(
      UUID courseId, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        get(CourseConstants.COURSE_ENDPOINT + "/{id}/tee", courseId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions getTee(
      UUID teeId, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        get(TeeConstants.TEE_BY_ID_ENDPOINT, teeId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions createTee(
      UUID courseId,
      String requestBody,
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        post(CourseConstants.COURSE_ENDPOINT + "/{id}/tee", courseId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions patchTee(
      UUID teeId,
      String requestBody,
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        patch(TeeConstants.TEE_BY_ID_ENDPOINT, teeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions deleteTee(
      UUID teeId, Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        delete(TeeConstants.TEE_BY_ID_ENDPOINT, teeId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }
}
