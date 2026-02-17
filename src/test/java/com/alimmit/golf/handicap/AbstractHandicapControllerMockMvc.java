package com.alimmit.golf.handicap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;
import java.util.function.Function;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

abstract class AbstractHandicapControllerMockMvc extends AbstractControllerMockMvc {

  AbstractHandicapControllerMockMvc(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions getMyHandicap(Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {

    return performMockMvc(
        get(HandicapConstants.HANDICAP_ENDPOINT).with(jwt().jwt(fn::apply)), expect);
  }

  ResultActions getMyHandicapHistory(Function<Jwt.Builder, Jwt.Builder> fn, ResultMatcher... expect)
      throws Exception {

    return performMockMvc(
        get(HandicapConstants.HANDICAP_ENDPOINT + GlobalConstants.API_HISTORY_SUFFIX)
            .with(jwt().jwt(fn::apply)),
        expect);
  }
}
