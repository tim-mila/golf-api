package com.alimmit.golf;

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
      MockHttpServletRequestBuilder builder,
      ResultMatcher... resultMatchers) throws Exception {
    return mockMvc.perform(builder).andExpectAll(resultMatchers);
  }
}
