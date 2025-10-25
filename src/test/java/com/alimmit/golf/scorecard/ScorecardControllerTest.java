package com.alimmit.golf.scorecard;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alimmit.golf.courses.client.GolfCourseApiClient;
import com.alimmit.golf.utils.JwtPersonas;

@ActiveProfiles("test")
@WebMvcTest
@Import({ ScorecardIdGenerator.class })
class ScorecardControllerTest {

  private final MockMvc mockMvc;

  @MockitoBean
  private GolfCourseApiClient golfCourseApiClient;

  @Autowired
  ScorecardControllerTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void addScorecard() throws Exception {

    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(JwtPersonas::forGaryGolfer))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseId").value(1),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.createdBy").value("123"),
            jsonPath("$.lastModifiedBy").value("123"),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }

  @Test
  void canOnlySeeMyScorecards() throws Exception {

    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    // Create scorecard for Gary Golfer
    this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(JwtPersonas::forGaryGolfer))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseId").value(1),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.createdBy").value("123"),
            jsonPath("$.lastModifiedBy").value("123"),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());

    // Fetch scorecards for Gary Golfer and expect one result
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(JwtPersonas::forGaryGolfer)))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(1));

    // Fetch scorecards for Pat Putter and expect empty result
    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(JwtPersonas::forPatPutter)))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(0));
  }
}
