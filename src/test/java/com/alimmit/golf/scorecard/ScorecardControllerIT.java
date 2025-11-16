package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Function;

import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
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

  @Autowired
  ScorecardControllerIT(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void addScorecard() throws Exception {
    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";
    createAndAssertScorecard(JwtPersona::forGaryGolfer, requestBody);
  }

  @Test
  void canOnlyListMyScorecards() throws Exception {

    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    // Create scorecard for Gary Golfer
    createAndAssertScorecard(JwtPersona::forGaryGolfer, requestBody);

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

    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    // Create scorecard for Gary Golfer and get response body to parse scorecardId
    String responseBody = createAndAssertScorecard(JwtPersona::forGaryGolfer, requestBody)
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

    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    // Create scorecard for Gary Golfer
    String responseBody = createAndAssertScorecard(JwtPersona::forGaryGolfer, requestBody)
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

  private ResultActions createAndAssertScorecard(Function<Jwt.Builder, Jwt.Builder> fn, String requestBody)
      throws Exception {
    return createScorecard(fn, requestBody, status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseId").value(1),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.getSub()),
            jsonPath("$.lastModifiedBy").value(JwtPersona.GARY_GOLFER
                .getSub()),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }
}
