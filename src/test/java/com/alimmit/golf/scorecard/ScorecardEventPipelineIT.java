package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.handicap.HandicapService;
import com.alimmit.golf.utils.JwtPersona;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(OutputCaptureExtension.class)
class ScorecardEventPipelineIT extends AbstractScorecardControllerMockMvc {

  @MockitoBean private HandicapService handicapService;

  @Autowired
  ScorecardEventPipelineIT(MockMvc mockMvc) {
    super(mockMvc);
  }

  private static final String REQUEST_BODY =
      """
      {"scoreDate": "2025-09-21", "courseName": "Test Course", "teeName": "Blue", "score": 88, "par": 72, "rating": 72.1, "slope": 125.0, "scorecardType": "EIGHTEEN"}
      """;

  @Test
  void createScorecard_triggersHandicapRecalculation(CapturedOutput output) throws Exception {
    createScorecard(JwtPersona.forGaryGolfer(), REQUEST_BODY, status().isCreated());

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              assertThat(output.toString()).contains("scorecardEvent | event received");
              verify(handicapService).calculate(anyList(), eq(JwtPersona.GARY_GOLFER.sub()));
            });
  }

  @Test
  void deleteScorecard_triggersHandicapRecalculation(CapturedOutput output) throws Exception {
    String responseBody =
        createScorecard(JwtPersona.forGaryGolfer(), REQUEST_BODY, status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID scorecardId =
        UUID.fromString(new ObjectMapper().readTree(responseBody).at("/scorecardId").asText());

    // Wait for the CREATED event to settle before issuing the DELETE
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                verify(handicapService, times(1))
                    .calculate(anyList(), eq(JwtPersona.GARY_GOLFER.sub())));

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId, status().isNoContent());

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              assertThat(output.toString())
                  .contains(
                      "ScorecardEvent[userId=" + JwtPersona.GARY_GOLFER.sub() + ", type=DELETED]");
              verify(handicapService, times(2))
                  .calculate(anyList(), eq(JwtPersona.GARY_GOLFER.sub()));
            });
  }
}
