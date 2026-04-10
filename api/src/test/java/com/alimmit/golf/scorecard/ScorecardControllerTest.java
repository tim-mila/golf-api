package com.alimmit.golf.scorecard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.utils.JwtPersona;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ActiveProfiles("test")
@WebMvcTest(ScorecardController.class)
@Import(MethodSecurityConfiguration.class)
class ScorecardControllerTest extends AbstractScorecardControllerMockMvc {

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

  // --- List scorecard tests ---

  @Test
  void listScorecards_DefaultPage() throws Exception {
    ScorecardDto mockDto = scorecardDto(UUID.randomUUID(), LocalDate.of(2025, 9, 21));
    when(scorecardService.list(eq(20), isNull()))
        .thenReturn(new ScorecardPageDto(List.of(mockDto), null, false));

    listScorecards(JwtPersona.forGaryGolfer(), status().isOk())
        .andExpectAll(
            jsonPath("$.data.length()").value(1),
            jsonPath("$.data[0].scorecardId").value(mockDto.scorecardId().toString()),
            jsonPath("$.hasNext").value(false),
            jsonPath("$.nextCursor").doesNotExist());
  }

  @Test
  void listScorecards_WithExplicitLimit() throws Exception {
    ScorecardDto mockDto = scorecardDto(UUID.randomUUID(), LocalDate.of(2025, 9, 21));
    when(scorecardService.list(eq(5), isNull()))
        .thenReturn(new ScorecardPageDto(List.of(mockDto), null, false));

    listScorecards(JwtPersona.forGaryGolfer(), 5, null, status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  void listScorecards_WithCursor_ReturnsNextPage() throws Exception {
    ScorecardCursor cursor = new ScorecardCursor(LocalDate.of(2025, 6, 1), UUID.randomUUID());
    String encodedCursor = cursor.encode();

    ScorecardDto mockDto = scorecardDto(UUID.randomUUID(), LocalDate.of(2025, 5, 15));
    when(scorecardService.list(eq(20), any(ScorecardCursor.class)))
        .thenReturn(new ScorecardPageDto(List.of(mockDto), null, false));

    listScorecards(JwtPersona.forGaryGolfer(), 20, encodedCursor, status().isOk())
        .andExpectAll(jsonPath("$.data.length()").value(1), jsonPath("$.hasNext").value(false));
  }

  @Test
  void listScorecards_HasNextPage_ReturnsNextCursor() throws Exception {
    ScorecardDto mockDto = scorecardDto(UUID.randomUUID(), LocalDate.of(2025, 9, 21));
    String nextCursor =
        new ScorecardCursor(LocalDate.of(2025, 9, 21), mockDto.scorecardId()).encode();
    when(scorecardService.list(eq(20), isNull()))
        .thenReturn(new ScorecardPageDto(List.of(mockDto), nextCursor, true));

    listScorecards(JwtPersona.forGaryGolfer(), status().isOk())
        .andExpectAll(
            jsonPath("$.hasNext").value(true), jsonPath("$.nextCursor").value(nextCursor));
  }

  @Test
  void listScorecards_InvalidCursor_Expect400() throws Exception {
    listScorecards(JwtPersona.forGaryGolfer(), 20, "not-valid-base64!!!", status().isBadRequest());
  }

  @Test
  void listScorecards_TamperedCursor_Expect400() throws Exception {
    // Valid base64url but content that decodes to garbage JSON — simulates a tampered token
    String tampered =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("tampered-payload".getBytes());
    listScorecards(JwtPersona.forGaryGolfer(), 20, tampered, status().isBadRequest());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 101})
  void listScorecards_InvalidLimit_Expect400(int limit) throws Exception {
    listScorecards(JwtPersona.forGaryGolfer(), limit, null, status().isBadRequest());
  }

  // --- Get / Delete tests ---

  @Test
  void getScorecardById() throws Exception {
    UUID scorecardId = UUID.randomUUID();
    ScorecardDto mockDto = scorecardDto(scorecardId, LocalDate.of(2025, 9, 21));

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

    when(scorecardService.deleteById(scorecardId)).thenReturn(1);

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId).andExpect(status().isNoContent());
  }

  @Test
  void deleteScorecardNotFound() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    when(scorecardService.deleteById(scorecardId)).thenReturn(0);

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId).andExpect(status().isNotFound());
  }

  @Test
  void deleteScorecardAmbiguous_ExpectInternalServerError() throws Exception {
    UUID scorecardId = UUID.randomUUID();

    when(scorecardService.deleteById(scorecardId)).thenReturn(2);

    deleteScorecard(JwtPersona.forGaryGolfer(), scorecardId)
        .andExpect(status().isInternalServerError());
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
    listScorecards(JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
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
    ScorecardDto mockDto = scorecardDto(scorecardId, LocalDate.of(2025, 9, 21));
    when(scorecardService.create(any(ScorecardRequestDto.class))).thenReturn(mockDto);

    createScorecard(JwtPersona.forGaryGolfer(), requestBody).andExpect(status().isCreated());
  }

  @Test
  void deleteScorecardWithAllowedScope_ExpectNoContent() throws Exception {
    UUID scorecardId = UUID.randomUUID();
    when(scorecardService.deleteById(scorecardId)).thenReturn(1);
    deleteScorecard(JwtPersona.forGaryGolfer(JwtPersona.SCOPE_WRITE_SCORECARD), scorecardId)
        .andExpect(status().isNoContent());
  }

  // --- Unauthenticated tests ---

  @ParameterizedTest
  @MethodSource
  void unauthenticated_ExpectUnauthorized(MockHttpServletRequestBuilder request) throws Exception {
    performWithoutAuth(request).andExpect(status().isUnauthorized());
  }

  static Stream<MockHttpServletRequestBuilder> unauthenticated_ExpectUnauthorized() {
    UUID id = UUID.randomUUID();
    return Stream.of(
        post(ScorecardConstants.SCORECARD_ENDPOINT).with(csrf()),
        get(ScorecardConstants.SCORECARD_ENDPOINT),
        get(ScorecardConstants.SCORECARD_ENDPOINT + "/{id}", id),
        delete(ScorecardConstants.SCORECARD_ENDPOINT + "/{id}", id).with(csrf()));
  }

  // --- HttpMessageNotReadableException (invalid enum) ---

  @Test
  void createScorecard_InvalidEnum_Expect400() throws Exception {
    String requestBody =
        "{\"scoreDate\":\"2024-01-01\",\"courseName\":\"Test\",\"teeName\":\"blue\",\"score\":85,\"par\":72,\"rating\":72.1,\"slope\":125.0,\"scorecardType\":\"BOGUS\"}";
    createScorecard(JwtPersona.forGaryGolfer(), requestBody).andExpect(status().isBadRequest());
  }

  // --- Request body validation tests ---

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

  // --- Helpers ---

  private ScorecardDto scorecardDto(UUID scorecardId, LocalDate scoreDate) {
    return new ScorecardDto(
        scorecardId,
        Instant.now(),
        JwtPersona.GARY_GOLFER.sub(),
        scoreDate,
        "Test Course",
        "Test Tee",
        88,
        72,
        72.1,
        125.0,
        ScorecardType.EIGHTEEN,
        14.4,
        true);
  }
}
