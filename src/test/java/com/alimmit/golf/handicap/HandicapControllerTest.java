package com.alimmit.golf.handicap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.courses.client.GolfCourseApiClient;
import com.alimmit.golf.scorecard.ScorecardService;
import com.alimmit.golf.utils.JwtPersona;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(HandicapController.class)
@Import(MethodSecurityConfiguration.class)
class HandicapControllerTest extends AbstractHandicapControllerMockMvc {

  @MockitoBean private GolfCourseApiClient golfCourseApiClient;

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
    super.getMyHandicap(JwtPersona::forGaryGolfer)
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
    super.getMyHandicap(JwtPersona::forGaryGolfer).andExpect(status().isNotFound());
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

    super.getMyHandicapHistory(JwtPersona::forGaryGolfer).andExpect(status().isOk());
  }

  // --- Authorization tests ---

  @Test
  void getHandicapForbiddenWithoutHandicapScope() throws Exception {
    super.getMyHandicap(JwtPersona::forGaryGolferNoHandicapScope)
        .andExpect(status().isForbidden());
  }

  @Test
  void getHandicapHistoryForbiddenWithoutHandicapScope() throws Exception {
    super.getMyHandicapHistory(JwtPersona::forGaryGolferNoHandicapScope)
        .andExpect(status().isForbidden());
  }
}
