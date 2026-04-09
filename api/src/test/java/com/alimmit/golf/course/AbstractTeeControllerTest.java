package com.alimmit.golf.course;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.utils.JwtClaimApplier;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

class AbstractTeeControllerTest extends AbstractControllerMockMvc {

  protected AbstractTeeControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions listTeesByCourse(UUID courseId, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn, get(TeeConstants.TEE_ENDPOINT).param("courseId", courseId.toString()), expect);
  }

  ResultActions getTee(UUID teeId, JwtClaimApplier fn, ResultMatcher... expect) throws Exception {
    return performMockMvc(fn, get(TeeConstants.TEE_BY_ID_ENDPOINT, teeId), expect);
  }

  ResultActions createTee(String requestBody, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        post(TeeConstants.TEE_ENDPOINT)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody),
        expect);
  }

  ResultActions patchTee(
      UUID teeId, String requestBody, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        patch(TeeConstants.TEE_BY_ID_ENDPOINT, teeId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody),
        expect);
  }

  ResultActions deleteTee(UUID teeId, JwtClaimApplier fn, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(fn, delete(TeeConstants.TEE_BY_ID_ENDPOINT, teeId), expect);
  }
}
