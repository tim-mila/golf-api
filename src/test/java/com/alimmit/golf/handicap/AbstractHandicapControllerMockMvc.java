package com.alimmit.golf.handicap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.alimmit.golf.AbstractControllerMockMvc;
import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.utils.JwtClaimApplier;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

abstract class AbstractHandicapControllerMockMvc extends AbstractControllerMockMvc {

  AbstractHandicapControllerMockMvc(MockMvc mockMvc) {
    super(mockMvc);
  }

  ResultActions getMyHandicap(JwtClaimApplier fn, ResultMatcher... expect) throws Exception {

    return performMockMvc(fn, get(HandicapConstants.HANDICAP_ENDPOINT), expect);
  }

  ResultActions getMyHandicapHistory(JwtClaimApplier fn, ResultMatcher... expect) throws Exception {

    return performMockMvc(
        fn, get(HandicapConstants.HANDICAP_ENDPOINT + GlobalConstants.API_HISTORY_SUFFIX), expect);
  }
}
