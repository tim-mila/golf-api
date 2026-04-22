package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtClaimApplier;
import com.alimmit.golf.utils.JwtPersona;
import java.util.HashSet;
import java.util.Set;
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

  private final ObjectMapper objectMapper;

  @Autowired
  ScorecardControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
    super(mockMvc);
    this.objectMapper = objectMapper;
  }

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
        .andExpect(jsonPath("$.data.length()").value(1));

    // Fetch scorecards for Pat Putter and expect empty result
    listScorecards(JwtPersona.forPatPutter(), status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void listScorecards_Pagination() throws Exception {
    // Create 3 scorecards for Gary Golfer
    createAndAssertScorecard(JwtPersona.forGaryGolfer());
    createAndAssertScorecard(JwtPersona.forGaryGolfer());
    createAndAssertScorecard(JwtPersona.forGaryGolfer());

    // Page 1: limit=2, expect 2 results and hasNext=true
    String page1Body =
        listScorecards(JwtPersona.forGaryGolfer(), 2, null, status().isOk())
            .andExpectAll(
                jsonPath("$.data.length()").value(2),
                jsonPath("$.nextCursor").isString())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String nextCursor = objectMapper.readTree(page1Body).at("/nextCursor").asText();
    assertThat(nextCursor).isNotBlank();

    // Page 2: use cursor from page 1, expect 1 result and hasNext=false
    String page2Body =
        listScorecards(JwtPersona.forGaryGolfer(), 2, nextCursor, status().isOk())
            .andExpectAll(
                jsonPath("$.data.length()").value(1),
                jsonPath("$.nextCursor").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Assert no overlap between pages
    Set<String> page1Ids = scorecardIds(page1Body);
    Set<String> page2Ids = scorecardIds(page2Body);
    assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
    assertThat(page1Ids).hasSize(2);
    assertThat(page2Ids).hasSize(1);
  }

  private java.util.Set<String> scorecardIds(String responseBody) throws Exception {
    Set<String> ids = new HashSet<>();
    objectMapper
        .readTree(responseBody)
        .at("/data")
        .forEach(n -> ids.add(n.at("/scorecardId").asText()));
    return ids;
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
        UUID.fromString(objectMapper.readTree(responseBody).at("/scorecardId").asString());
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
        UUID.fromString(objectMapper.readTree(responseBody).at("/scorecardId").asString());
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
