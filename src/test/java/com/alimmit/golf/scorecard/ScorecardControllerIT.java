package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtClaimApplier;
import com.alimmit.golf.utils.JwtPersona;
import java.util.UUID;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ScorecardControllerIT extends AbstractScorecardControllerMockMvc {

  @MockitoBean private ScorecardEventPublisher scorecardEventPublisher;

  @Autowired
  ScorecardControllerIT(MockMvc mockMvc) {
    super(mockMvc);
  }

  // , "rating": 72.1, "slope": 125
  private static final String REQUEST_BODY =
      """
      {"scoreDate": "2025-09-21", "courseName": "Test Course", "teeName": "Blue", "score": 88, "par": 72, "rating": 72.1, "slope": 125.0, "scorecardType": "EIGHTEEN"}
      """;

  @Test
  void addScorecard() throws Exception {
    createAndAssertScorecard(JwtPersona.forGaryGolfer());

    Mockito.verify(scorecardEventPublisher, Mockito.times(1))
        .publishCreated(Mockito.any(ScorecardDto.class));
  }

  @Test
  void canOnlyListMyScorecards() throws Exception {

    // Create scorecard for Gary Golfer
    createAndAssertScorecard(JwtPersona.forGaryGolfer());

    // Fetch scorecards for Gary Golfer and expect one result
    listScorecards(JwtPersona.forGaryGolfer(), status().isOk())
        .andExpectAll(jsonPath("$.length()").value(1));

    // Fetch scorecards for Pat Putter and expect empty result
    listScorecards(JwtPersona.forPatPutter(), status().isOk())
        .andExpectAll(jsonPath("$.length()").value(0));
  }

  @Test
  void canOnlyFetchMyScorecards() throws Exception {
    // Create scorecard for Gary Golfer and get response body to parse scorecardId
    String responseBody =
        createAndAssertScorecard(JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID scorecardId =
        UUID.fromString(new ObjectMapper().readTree(responseBody).at("/scorecardId").asString());
    assertThat(scorecardId).isNotNull();

    // Fetch scorecards for Gary Golfer and expect ok
    getScorecard(JwtPersona.forGaryGolfer(), scorecardId, status().isOk());

    // Fetch scorecards for Pat Putter and expect not found
    getScorecard(JwtPersona.forPatPutter(), scorecardId, status().isNotFound());
  }

  @Test
  void canOnlyDeleteMyScorecards() throws Exception {
    // Create scorecard for Gary Golfer
    String responseBody =
        createAndAssertScorecard(JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID scorecardId =
        UUID.fromString(new ObjectMapper().readTree(responseBody).at("/scorecardId").asString());
    assertThat(scorecardId).isNotNull();

    // Delete scorecards as Pat Putter and expect not found
    deleteScorecard(JwtPersona.forPatPutter(), scorecardId, status().isNotFound());

    // Delete scorecard as Gary Golfer and expect ok
    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId, status().isNoContent());

    // Delete scorecard as Gary Golfer again and expect not found
    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId, status().isNotFound());
  }

  private ResultActions createAndAssertScorecard(JwtClaimApplier fn) throws Exception {
    return createScorecard(fn, REQUEST_BODY, status().isCreated())
        .andExpectAll(
            jsonPath("$.scorecardId")
                .value(
                    MatchesPattern.matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")),
            jsonPath("$.courseName").value("Test Course"),
            jsonPath("$.teeName").value("Blue"),
            jsonPath("$.score").value(88),
            jsonPath("$.par").value(72),
            jsonPath("$.courseRating").value(72.1),
            jsonPath("$.slopeRating").value(125.0),
            jsonPath("$.scorecardType").value("EIGHTEEN"),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("$.createdAt").isString());
  }
}
