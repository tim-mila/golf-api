package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Function;

import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.utils.JwtPersona;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup-scorecard.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ScorecardControllerIT {

  private final MockMvc mockMvc;

  @Autowired
  ScorecardControllerIT(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
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
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(1));

    // Fetch scorecards for Pat Putter and expect empty result
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forPatPutter)))
        .andExpect(status().isOk())
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
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isOk());

    // Fetch scorecards for Pat Putter and expect not found
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forPatPutter)))
        .andExpect(status().isNotFound());
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
    this.mockMvc.perform(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forPatPutter)))
        .andExpect(status().isNotFound());

    // Delete scorecard as Gary Golfer and expect ok
    this.mockMvc.perform(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isNoContent());

    // Delete scorecard as Gary Golfer again and expect not found
    this.mockMvc.perform(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isNotFound());
  }

  private ResultActions createAndAssertScorecard(Function<Jwt.Builder, Jwt.Builder> fn, String requestBody)
      throws Exception {
    return this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(fn::apply))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
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
