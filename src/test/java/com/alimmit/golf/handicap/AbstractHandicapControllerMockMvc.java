package com.alimmit.golf.handicap;

import com.alimmit.golf.AbstractControllerMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.function.Function;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

abstract class AbstractHandicapControllerMockMvc extends AbstractControllerMockMvc {

  AbstractHandicapControllerMockMvc(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions getMyHandicap(
      Function<Jwt.Builder, Jwt.Builder> fn,
      ResultMatcher... expect) throws Exception {

    return performMockMvc(
        get(HandicapConstants.HANDICAP_ENDPOINT)
            .with(jwt().jwt(fn::apply)),
        expect);
  }
}
