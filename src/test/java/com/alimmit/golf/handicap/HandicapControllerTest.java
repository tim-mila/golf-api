package com.alimmit.golf.handicap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.scorecard.ScorecardService;
import com.alimmit.golf.utils.JwtPersona;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("test")
@WebMvcTest(HandicapController.class)
@Import(MethodSecurityConfiguration.class)
class HandicapControllerTest extends AbstractHandicapControllerMockMvc {

  @MockitoBean private ScorecardService scorecardService;

  @MockitoBean private HandicapService handicapService;

  @Autowired
  HandicapControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void getMyHandicap() throws Exception {
    when(handicapService.getHandicap())
        .thenReturn(
            Optional.of(
                new HandicapDto(
                    UUID.fromString("10000000-0000-0000-0000-000000000000"),
                    JwtPersona.GARY_GOLFER.sub(),
                    Instant.now(),
                    Instant.now(),
                    10.2,
                    8,
                    20)));
    super.getMyHandicap(JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("handicapId").value("10000000-0000-0000-0000-000000000000"),
            jsonPath("handicapIndex").value(10.2),
            jsonPath("roundsUsed").value(8),
            jsonPath("totalRounds").value(20),
            jsonPath("golferId").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("createdAt").exists());
  }

  @Test
  void getMyHandicap_ExpectMissing() throws Exception {
    when(handicapService.getHandicap()).thenReturn(Optional.empty());
    super.getMyHandicap(JwtPersona.forGaryGolfer()).andExpect(status().isNoContent());
  }

  @Test
  void getMyHandicapHistory() throws Exception {

    when(handicapService.getHistory())
        .thenReturn(
            List.of(
                new HandicapRevisionDto(
                    UUID.fromString("10000000-0000-0000-0000-000000000000"),
                    JwtPersona.GARY_GOLFER.sub(),
                    Instant.now(),
                    Instant.now(),
                    10.2,
                    8,
                    20,
                    1,
                    RevisionMetadata.RevisionType.INSERT,
                    Instant.now())));

    super.getMyHandicapHistory(JwtPersona.forGaryGolfer()).andExpect(status().isOk());
  }

  // --- Unauthenticated tests ---

  @ParameterizedTest
  @MethodSource
  void unauthenticated_ExpectUnauthorized(MockHttpServletRequestBuilder request) throws Exception {
    performWithoutAuth(request).andExpect(status().isUnauthorized());
  }

  static Stream<MockHttpServletRequestBuilder> unauthenticated_ExpectUnauthorized() {
    return Stream.of(
        get(HandicapConstants.HANDICAP_ENDPOINT),
        get(HandicapConstants.HANDICAP_ENDPOINT + GlobalConstants.API_HISTORY_SUFFIX));
  }

  // --- Authorization tests ---

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        "",
      })
  void getHandicapWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    super.getMyHandicap(JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        "",
      })
  void getHandicapHistoryWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    super.getMyHandicapHistory(JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
  }
}
