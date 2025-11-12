package com.alimmit.golf.scorecard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.hamcrest.text.MatchesPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.courses.client.GolfCourseApiClient;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.utils.JwtPersona;

@ActiveProfiles("test")
@WebMvcTest
class ScorecardControllerTest {

  private final MockMvc mockMvc;

  @MockitoBean
  private GolfCourseApiClient golfCourseApiClient;

  @MockitoBean
  private ScorecardService scorecardService;

  @Autowired
  ScorecardControllerTest(MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void createScorecard() throws Exception {
    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"courseId\": 1, \"score\": 88}";

    ScorecardDto mockDto = new ScorecardDto(
        "scr-" + "a".repeat(32),
        Instant.now(),
        JwtPersona.GARY_GOLFER.getSub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.getSub(),
        LocalDate.of(2025, 9, 21),
        1L,
        88);

    when(scorecardService.create(any(ScorecardRequestDto.class))).thenReturn(mockDto);

    this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forGaryGolfer))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(MatchesPattern.matchesPattern("^(scr-)[a-zA-Z0-9]{32}$")),
            jsonPath("$.courseId").value(1),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.score").value(88),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.getSub()),
            jsonPath("$.lastModifiedBy").value(JwtPersona.GARY_GOLFER.getSub()),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }

  @Test
  void listScorecards() throws Exception {
    ScorecardDto mockDto = new ScorecardDto(
        "scr-" + "a".repeat(32),
        Instant.now(),
        JwtPersona.GARY_GOLFER.getSub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.getSub(),
        LocalDate.of(2025, 9, 21),
        1L,
        88);

    when(scorecardService.listAll()).thenReturn(List.of(mockDto));

    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isOk())
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
        JwtPersona.GARY_GOLFER.getSub(),
        Instant.now(),
        JwtPersona.GARY_GOLFER.getSub(),
        LocalDate.of(2025, 9, 21),
        1L,
        88);

    when(scorecardService.getById(scorecardId)).thenReturn(mockDto);

    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.scorecardId").value(scorecardId));
  }

  @Test
  void getScorecardByIdNotFound() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    when(scorecardService.getById(scorecardId)).thenThrow(new NotFoundException());

    this.mockMvc.perform(
        get(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isNotFound());
  }

  @Test
  void deleteScorecard() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    // doNothing() is default for void methods, so no need to mock success case

    this.mockMvc.perform(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteScorecardNotFound() throws Exception {
    String scorecardId = "scr-" + "a".repeat(32);

    doThrow(new NotFoundException()).when(scorecardService).deleteById(scorecardId);

    this.mockMvc.perform(
        delete(ScorecardConstants.SCORECARD_ENDPOINT + GlobalConstants.API_RECORD_SUFFIX, scorecardId)
            .with(jwt().jwt(JwtPersona::forGaryGolfer)))
        .andExpect(status().isNotFound());
  }

  @Test
  void createWithMissingCourseAndExpectBadRequest() throws Exception {
    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"score\": 88}";

    this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forGaryGolfer))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createWithMissingScoreAndExpectBadRequest() throws Exception {
    String requestBody = "{\"scoreDate\": \"2025-09-21\", \"coureseId\": 1}";

    this.mockMvc.perform(
        post(ScorecardConstants.SCORECARD_ENDPOINT)
            .with(jwt().jwt(JwtPersona::forGaryGolfer))
            .content(requestBody)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
