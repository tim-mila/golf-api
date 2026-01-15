package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Function;

import com.alimmit.golf.courses.client.GolfCourseApiClient;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.alimmit.golf.utils.JwtPersona;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup-scorecard.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ScorecardControllerIT extends AbstractScorecardControllerMockMvc {

  @MockitoBean
  private GolfCourseApiClient golfCourseApiClient;

  @Autowired
  ScorecardControllerIT(MockMvc mockMvc) {
    super(mockMvc);
  }

  // , "rating": 72.1, "slope": 125
  private static final String REQUEST_BODY = """
      {"scoreDate": "2025-09-21", "courseName": "Test Course", "teeName": "Blue", "score": 88, "rating": 72.1, "slope": 125.0}
      """;

  @Test
  void addScorecard() throws Exception {
    createAndAssertScorecard(JwtPersona::forGaryGolfer);
  }

  @Test
  void canOnlyListMyScorecards() throws Exception {

    // Create scorecard for Gary Golfer
    createAndAssertScorecard(JwtPersona::forGaryGolfer);

    // Fetch scorecards for Gary Golfer and expect one result
    listScorecards(JwtPersona::forGaryGolfer, status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(1));

    // Fetch scorecards for Pat Putter and expect empty result
    listScorecards(JwtPersona::forPatPutter, status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(0));
  }

  @Test
  void canOnlyFetchMyScorecards() throws Exception {
    // Create scorecard for Gary Golfer and get response body to parse scorecardId
    String responseBody = createAndAssertScorecard(JwtPersona::forGaryGolfer)
        .andReturn().getResponse().getContentAsString();

    String scorecardId = new ObjectMapper().readTree(responseBody).at("/scorecardId").asText();
    assertThat(scorecardId).isNotBlank();

    // Fetch scorecards for Gary Golfer and expect ok
    getScorecard(JwtPersona::forGaryGolfer, scorecardId, status().isOk());

    // Fetch scorecards for Pat Putter and expect not found
    getScorecard(JwtPersona::forPatPutter, scorecardId, status().isNotFound());
  }

  @Test
  void canOnlyDeleteMyScorecards() throws Exception {
    // Create scorecard for Gary Golfer
    String responseBody = createAndAssertScorecard(JwtPersona::forGaryGolfer)
        .andReturn().getResponse().getContentAsString();

    String scorecardId = new ObjectMapper().readTree(responseBody).at("/scorecardId").asText();
    assertThat(scorecardId).isNotBlank();

    // Delete scorecards as Pat Putter and expect not found
    deleteScorecard(JwtPersona::forPatPutter, scorecardId, status().isNotFound());

    // Delete scorecard as Gary Golfer and expect ok
    deleteScorecard(JwtPersona::forGaryGolfer, scorecardId, status().isNoContent());

    // Delete scorecard as Gary Golfer again and expect not found
    deleteScorecard(JwtPersona::forGaryGolfer, scorecardId, status().isNotFound());
  }

  private ResultActions createAndAssertScorecard(Function<Jwt.Builder, Jwt.Builder> fn)
      throws Exception {
    return createScorecard(fn, REQUEST_BODY, status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseName").value("Test Course"),
            jsonPath("$.score").value(88),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.getSub()),
            jsonPath("$.lastModifiedBy").value(JwtPersona.GARY_GOLFER
                .getSub()),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }
}
