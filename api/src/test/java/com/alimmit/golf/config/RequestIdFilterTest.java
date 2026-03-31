package com.alimmit.golf.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

  private RequestIdFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RequestIdFilter();
    MDC.clear();
  }

  @Test
  void usesProvidedRequestId() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "my-request-id");
    var response = new MockHttpServletResponse();
    var chain = capturingChain();

    filter.doFilterInternal(request, response, chain);

    assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("my-request-id");
    assertThat(chain.capturedRequestId).isEqualTo("my-request-id");
  }

  @Test
  void generatesRequestIdWhenHeaderAbsent() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();
    var chain = capturingChain();

    filter.doFilterInternal(request, response, chain);

    String responseId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
    assertThat(responseId).isNotBlank();
    assertThat(chain.capturedRequestId).isEqualTo(responseId);
  }

  @Test
  void generatesRequestIdWhenHeaderBlank() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "  ");
    var response = new MockHttpServletResponse();
    var chain = capturingChain();

    filter.doFilterInternal(request, response, chain);

    String responseId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
    assertThat(responseId).isNotBlank();
    assertThat(responseId).isNotEqualTo("  ");
  }

  @Test
  void clearsMdcAfterRequest() throws Exception {
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, new MockFilterChain());

    assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
  }

  private CapturingFilterChain capturingChain() {
    return new CapturingFilterChain();
  }

  private static class CapturingFilterChain extends MockFilterChain {
    String capturedRequestId;

    @Override
    public void doFilter(
        jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
        throws IOException, jakarta.servlet.ServletException {
      capturedRequestId = MDC.get(RequestIdFilter.MDC_KEY);
      super.doFilter(request, response);
    }
  }
}
