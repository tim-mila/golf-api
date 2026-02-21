package com.alimmit.golf;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.alimmit.golf.utils.JwtClaimApplier;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public abstract class AbstractControllerMockMvc {

  private final MockMvc mockMvc;

  protected AbstractControllerMockMvc(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  protected ResultActions performMockMvc(
      JwtClaimApplier fn, MockHttpServletRequestBuilder builder, ResultMatcher... resultMatchers)
      throws Exception {
    return mockMvc.perform(builder.with(jwt().jwt(fn::apply))).andExpectAll(resultMatchers);
  }
}
