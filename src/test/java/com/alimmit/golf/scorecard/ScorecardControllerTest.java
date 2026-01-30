package com.alimmit.golf.scorecard;

import com.alimmit.golf.courses.client.GolfCourseApiClient;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.utils.JwtPersona;
import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ScorecardController.class)
class ScorecardControllerTest extends AbstractScorecardControllerMockMvc {

  @MockitoBean
  private GolfCourseApiClient golfCourseApiClient;

  @MockitoBean
  private ScorecardService scorecardService;

  @MockitoBean
  private ScorecardEventPublisher scorecardEventPublisher;

  @Autowired
  ScorecardControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void createScorecard() throws Exception {
    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"rating\": 72.1, \"slope\": 125.0, \"holesPlayed\": 18}";

    ScorecardDto mockDto = new ScorecardDto(
        "scr-" + "a".repeat(32),
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        LocalDate.of(2025, 9, 21),
        "Test Course",
        88,
        72.1,
        125.0,
        18);

    when(scorecardService.create(any(ScorecardRequestDto.class))).thenReturn(mockDto);

    createScorecard(JwtPersona::forGaryGolfer, requestBody, status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseName").value("Test Course"),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.score").value(88),
            jsonPath("$.holesPlayed").value(18),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("$.lastModifiedBy").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }

  @Test
  void listScorecards() throws Exception {
    ScorecardDto mockDto = new ScorecardDto(
        "scr-" + "a".repeat(32),
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        LocalDate.of(2025, 9, 21),
        "Test Course",
        88,
        72.1,
        125.0,
        18);

    when(scorecardService.listAll()).thenReturn(List.of(mockDto));

    listScorecards(JwtPersona::forGaryGolfer, status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(1),
            jsonPath("$[0].scorecardId").value(mockDto.scorecardId()));
  }

  @Test
  void getScorecardById() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    ScorecardDto mockDto = new ScorecardDto(
        scorecardId,
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        LocalDate.of(2025, 9, 21),
        "Test Course",
        88,
        72.1,
        125.0,
        18);

    when(scorecardService.getById(scorecardId)).thenReturn(Optional.of(mockDto));

    getScorecard(JwtPersona::forGaryGolfer, scorecardId, status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(scorecardId));
  }

  @Test
  void getScorecardByIdNotFound() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    when(scorecardService.getById(scorecardId)).thenThrow(new NotFoundException());

    getScorecard(JwtPersona::forGaryGolfer, scorecardId)
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteScorecard() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    // doNothing() is default for void methods, so no need to mock success case
    when(scorecardService.deleteById(scorecardId)).thenReturn(1);

    deleteScorecard(JwtPersona::forGaryGolfer, scorecardId)
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteScorecardNotFound() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    when(scorecardService.deleteById(scorecardId)).thenReturn(0);

    deleteScorecard(JwtPersona::forGaryGolfer, scorecardId)
        .andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"scoreDate\": \"2025-09-21\", \"score\": 88}", // Missing course and tee, or courseName, slope, rating, and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\"}", // Missing tee, score, and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"holesPlayed\": 18}", // Missing tee, and score
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"tee\": \"Blue\"}", // Missing score and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"tee\": \"Blue\", \"holesPlayed\": 18}", // Missing score
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\"}", // Missing slope, rating, score, and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"holesPlayed\": 18}", // Missing slope, rating, and score
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"slope\": 72.0}", // Missing course rating, score, and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"slope\": 72.0, \"holesPlayed\": 18}", // Missing course rating, and score
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"slope\": 72.0, \"score\": 88}", // Missing course rating and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"slope\": 72.0, \"score\": 88, \"holesPlayed\": 18}", // Missing course rating
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0}", // Missing slope, score, and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0, \"holesPlayed\": 18}", // Missing slope, and score
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0, \"score\": 89}", // Missing slope and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0, \"score\": 89, \"holesPlayed\": 18}", // Missing slope
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0, \"slope\": 73.0}", // Missing score and holesPlayed
      "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test course\", \"rating\": 72.0, \"slope\": 73.0, \"holesPlayed\": 18}" // Missing score
  })
  void createAndExpectBadRequest(String requestBody) throws Exception {
    createScorecard(JwtPersona::forGaryGolfer, requestBody)
        .andExpect(status().isBadRequest());
  }
}
