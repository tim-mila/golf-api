package com.alimmit.golf.handicap;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import com.alimmit.golf.scorecard.ScorecardDto;
import com.alimmit.golf.scorecard.ScorecardType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
  TestJpaAuditingConfig.class,
  JpaHandicapServiceImpl.class,
  HandicapMapper.class,
  HandicapCalculatorImpl.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JpaHandicapServiceImplIT {

  private final HandicapService handicapService;

  @Autowired
  JpaHandicapServiceImplIT(HandicapService handicapService) {
    this.handicapService = handicapService;
  }

  @Test
  void contextLoads() {
    assertThat(handicapService).isNotNull();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void calculate_withInsufficientRounds_doesNotCreateHandicap() {
    // Only 2 rounds of 18 holes = 36 holes total, below the 54-hole minimum
    List<ScorecardDto> scorecards =
        List.of(
            createScorecard("123", ScorecardType.EIGHTEEN, 10.0),
            createScorecard("123", ScorecardType.EIGHTEEN, 12.0));

    handicapService.calculate(scorecards, "123");

    Optional<HandicapDto> result = handicapService.getHandicap();
    assertThat(result).isEmpty();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void calculate_thenGetHandicap() {
    handicapService.calculate(threeRoundScorecards("123"), "123");

    Optional<HandicapDto> result = handicapService.getHandicap();

    assertThat(result)
        .isPresent()
        .get()
        .hasFieldOrProperty("handicapId")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("golferId", "123")
        .hasFieldOrProperty("handicapIndex")
        .hasFieldOrPropertyWithValue("totalRounds", 3);
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void calculate_updatesExistingHandicap() {
    handicapService.calculate(threeRoundScorecards("123"), "123");
    HandicapDto initial = handicapService.getHandicap().orElseThrow();

    handicapService.calculate(sixRoundScorecards("123"), "123");
    HandicapDto updated = handicapService.getHandicap().orElseThrow();

    assertThat(updated)
        .hasFieldOrPropertyWithValue("handicapId", initial.handicapId())
        .hasFieldOrPropertyWithValue("golferId", "123")
        .hasFieldOrPropertyWithValue("totalRounds", 6);
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void getHistory_returnsRevisionHistory() {
    handicapService.calculate(threeRoundScorecards("123"), "123");
    handicapService.calculate(sixRoundScorecards("123"), "123");

    List<HandicapRevisionDto> history = handicapService.getHistory();

    assertThat(history).hasSize(2);
  }

  private List<ScorecardDto> threeRoundScorecards(String golferId) {
    return List.of(
        createScorecard(golferId, ScorecardType.EIGHTEEN, 10.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 12.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 14.0));
  }

  private List<ScorecardDto> sixRoundScorecards(String golferId) {
    return List.of(
        createScorecard(golferId, ScorecardType.EIGHTEEN, 10.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 12.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 14.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 11.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 13.0),
        createScorecard(golferId, ScorecardType.EIGHTEEN, 15.0));
  }

  private ScorecardDto createScorecard(String createdBy, ScorecardType type, Double differential) {
    return new ScorecardDto(
        UUID.randomUUID(),
        Instant.now(),
        createdBy,
        LocalDate.now(),
        "Test Club",
        "Blue",
        90,
        72,
        70.0,
        113.0,
        type,
        differential,
        false);
  }
}
