package com.alimmit.golf.scorecard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.courses.client.GolfCourseApiClient;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.utils.JwtPersona;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(ScorecardController.class)
@Import(MethodSecurityConfiguration.class)
class ScorecardControllerTest extends AbstractScorecardControllerMockMvc {

  @MockitoBean private GolfCourseApiClient golfCourseApiClient;

  @MockitoBean private ScorecardService scorecardService;

  @MockitoBean private ScorecardEventPublisher scorecardEventPublisher;

  @Autowired
  ScorecardControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void createScorecard() throws Exception {
    String requestBody =
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}";
    UUID scorecardId = UUID.randomUUID();
    ScorecardDto mockDto =
        new ScorecardDto(
            scorecardId,
            Instant.now(),
            JwtPersona.GARY_GOLFER.sub(),
            LocalDate.of(2025, 9, 21),
            "Test Course",
            "Test Tee",
            88,
            72,
            72.1,
            125.0,
            ScorecardType.EIGHTEEN,
            14.4,
            true);

    when(scorecardService.create(any(ScorecardRequestDto.class))).thenReturn(mockDto);

    createScorecard(JwtPersona.forGaryGolfer(), requestBody, status().isCreated())
        .andExpectAll(
            jsonPath("$.scorecardId").value(scorecardId.toString()),
            jsonPath("$.courseName").value("Test Course"),
            jsonPath("$.teeName").value("Test Tee"),
            jsonPath("$.scoreDate").value("2025-09-21"),
            jsonPath("$.score").value(88),
            jsonPath("$.courseRating").value(72.1),
            jsonPath("$.slopeRating").value(125.0),
            jsonPath("$.scorecardType").value("EIGHTEEN"),
            jsonPath("$.differential").value(14.4),
            jsonPath("$.indexEstablished").value(true),
            jsonPath("$.createdBy").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("$.createdAt").isString());
  }

  @Test
  void listScorecards() throws Exception {
    ScorecardDto mockDto =
        new ScorecardDto(
            UUID.randomUUID(),
            Instant.now(),
            JwtPersona.GARY_GOLFER.sub(),
            LocalDate.of(2025, 9, 21),
            "Test Course",
            "Test Tee",
            88,
            72,
            72.1,
            125.0,
            ScorecardType.EIGHTEEN,
            14.4,
            true);

    when(scorecardService.listAll()).thenReturn(List.of(mockDto));

    listScorecards(JwtPersona.forGaryGolfer(), status().isOk())
        .andExpectAll(
            jsonPath("$.length()").value(1),
            jsonPath("$[0].scorecardId").value(mockDto.scorecardId().toString()));
  }

  @Test
  void getScorecardById() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    ScorecardDto mockDto =
        new ScorecardDto(
            scorecardId,
            Instant.now(),
            JwtPersona.GARY_GOLFER.sub(),
            LocalDate.of(2025, 9, 21),
            "Test Course",
            "Test Tee",
            88,
            72,
            72.1,
            125.0,
            ScorecardType.EIGHTEEN,
            14.4,
            true);

    when(scorecardService.getById(scorecardId)).thenReturn(Optional.of(mockDto));

    getScorecard(JwtPersona.forGaryGolfer(), scorecardId, status().isOk())
        .andExpectAll(jsonPath("$.scorecardId").value(scorecardId.toString()));
  }

  @Test
  void getScorecardByIdNotFound() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    when(scorecardService.getById(scorecardId)).thenThrow(new NotFoundException());

    getScorecard(JwtPersona.forGaryGolfer(), scorecardId).andExpect(status().isNotFound());
  }

  @Test
  void deleteScorecard() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    // doNothing() is default for void methods, so no need to mock success case
    when(scorecardService.deleteById(scorecardId)).thenReturn(1);

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId).andExpect(status().isNoContent());
  }

  @Test
  void deleteScorecardNotFound() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    when(scorecardService.deleteById(scorecardId)).thenReturn(0);

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId).andExpect(status().isNotFound());
  }

  // --- Authorization tests ---

  @ParameterizedTest
  @ValueSource(strings = {JwtPersona.SCOPE_READ_SCORECARD, JwtPersona.SCOPE_READ_HANDICAP, ""})
  void createScorecardWithDisallowedScope_ExpectForbidden(String scope) throws Exception {
    String requestBody =
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}";
    createScorecard(JwtPersona.forGaryGolfer(scope), requestBody).andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(strings = {JwtPersona.SCOPE_WRITE_SCORECARD, JwtPersona.SCOPE_READ_HANDICAP, ""})
  void listScorecardsWithDisallowedScope_ExpectForbidden(String scope) throws Exception {
    when(scorecardService.listAll()).thenReturn(List.of());
    listScorecards(JwtPersona.forGaryGolfer(scope), status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(strings = {JwtPersona.SCOPE_WRITE_SCORECARD, JwtPersona.SCOPE_READ_HANDICAP, ""})
  void getScorecardWithDisallowedScope_ExpectForbidden(String scope) throws Exception {
    getScorecard(JwtPersona.forGaryGolfer(scope), UUID.randomUUID())
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(strings = {JwtPersona.SCOPE_READ_SCORECARD, JwtPersona.SCOPE_READ_HANDICAP, ""})
  void deleteScorecardWithDisallowedScope_ExpectForbidden(String scope) throws Exception {
    deleteScorecard(JwtPersona.forGaryGolfer(scope), UUID.randomUUID())
        .andExpect(status().isForbidden());
  }

  @Test
  void createScorecardWithAllowedScope_ExpectCreated() throws Exception {
    String requestBody =
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}";
    UUID scorecardId = UUID.randomUUID();
    ScorecardDto mockDto =
        new ScorecardDto(
            scorecardId,
            Instant.now(),
            JwtPersona.GARY_GOLFER.sub(),
            LocalDate.of(2025, 9, 21),
            "Test Course",
            "blue",
            88,
            72,
            72.1,
            125.0,
            ScorecardType.EIGHTEEN,
            14.4,
            true);
    when(scorecardService.create(any(ScorecardRequestDto.class))).thenReturn(mockDto);

    createScorecard(JwtPersona.forGaryGolfer(), requestBody, status().isCreated());
  }

  @Test
  void deleteScorecardWithAllowedScope_ExpectNoContent() throws Exception {
    UUID scorecardId = UUID.randomUUID();
    when(scorecardService.deleteById(scorecardId)).thenReturn(1);
    deleteScorecard(JwtPersona.forGaryGolfer(JwtPersona.SCOPE_WRITE_SCORECARD), scorecardId)
        .andExpect(status().isNoContent());
  }

  // --- Validation tests ---

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0}", // Missing scorecard type
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"scorecardType\": \"EIGHTEEN\"}", // Missing slope
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing rating
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing par
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing score
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing teeName
        "{\"scoreDate\": \"2025-09-21\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing courseName
        "{\"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 72.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Missing scoreDate
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 82.1, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}", // Rating above range
        "{\"scoreDate\": \"2025-09-21\", \"courseName\": \"Test Course\", \"teeName\": \"blue\", \"score\": 88, \"par\": 72, \"rating\": 61.9, \"slope\": 125.0, \"scorecardType\": \"EIGHTEEN\"}" // Rating below range
      })
  void createAndExpectBadRequest(String requestBody) throws Exception {
    createScorecard(JwtPersona.forGaryGolfer(), requestBody).andExpect(status().isBadRequest());
  }
}
