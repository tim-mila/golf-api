package com.alimmit.golf.scorecard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.utils.JwtClaimApplier;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

abstract class AbstractScorecardControllerMockMvc extends AbstractControllerMockMvc {

  protected AbstractScorecardControllerMockMvc(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions createScorecard(JwtClaimApplier fn, String requestBody, ResultMatcher... expect)
      throws Exception {
    return performMockMvc(
        fn,
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON),
        expect);
  }

  ResultActions listScorecards(JwtClaimApplier fn, ResultMatcher... expect) throws Exception {

    return performMockMvc(fn, get(ScorecardConstants.SCORECARD_ENDPOINT), expect);
  }

  ResultActions getScorecard(JwtClaimApplier fn, UUID scorecardId, ResultMatcher... expect)
      throws Exception {

    return performMockMvc(
        fn,
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId),
        expect);
  }

  ResultActions deleteScorecard(JwtClaimApplier fn, UUID scorecardId, ResultMatcher... expect)
      throws Exception {

    return performMockMvc(
        fn,
        delete(
            ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId),
        expect);
  }
}
