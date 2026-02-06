package com.alimmit.golf.scorecard;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.UUID;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;

abstract class AbstractScorecardControllerMockMvc extends AbstractControllerMockMvc {

  protected AbstractScorecardControllerMockMvc(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions createScorecard(
      Function<Jwt.Builder, Jwt.Builder> fn,
      String requestBody,
      ResultMatcher... expect) throws Exception {
    return performWithToken(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON),
        fn,
        expect);
  }

  ResultActions listScorecards(
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect) throws Exception {

    return performMockMvc(get(ScorecardConstants.SCORECARD_ENDPOINT).with(jwt().jwt(fn::apply)), expect);
  }

  ResultActions getScorecard(
      Function<Jwt.Builder, Jwt.Builder> fn,
      UUID scorecardId,
      ResultMatcher... expect) throws Exception {

    return performMockMvc(
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions deleteScorecard(
      Function<Jwt.Builder, Jwt.Builder> fn,
      UUID scorecardId,
      ResultMatcher... expect) throws Exception {

    return performMockMvc(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(fn::apply)),
        expect);
  }

  ResultActions performWithToken(
      MockHttpServletRequestBuilder builder,
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect) throws Exception {
    return performMockMvc(builder.with(jwt().jwt(fn::apply)), expect);
  }
}
