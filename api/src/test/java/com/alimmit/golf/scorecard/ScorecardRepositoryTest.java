package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import com.alimmit.golf.utils.JwtPersona;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaAuditingConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class ScorecardRepositoryTest {

  private final ScorecardRepository scorecardRepository;

  @Autowired
  ScorecardRepositoryTest(ScorecardRepository scorecardRepository) {
    this.scorecardRepository = scorecardRepository;
  }

  @Test
  @WithMockUser(username = "123") // Gary Golfer's sub
  void shouldSaveEntityWithAuditFields() {
    // Given: Gary Golfer is authenticated

    // When: Creating a new scorecard
    ScorecardEntity entity =
        new ScorecardEntity(
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
    ScorecardEntity saved = scorecardRepository.save(entity);

    // Then: Audit fields should be populated automatically
    Assertions.assertThat(saved)
        .hasFieldOrProperty("id")
        .hasFieldOrPropertyWithValue("createdBy", JwtPersona.GARY_GOLFER.sub())
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("courseName", "Test Course")
        .hasFieldOrPropertyWithValue("teeName", "Test Tee")
        .hasFieldOrPropertyWithValue("score", 88)
        .hasFieldOrPropertyWithValue("par", 72)
        .hasFieldOrPropertyWithValue("rating", 72.1)
        .hasFieldOrPropertyWithValue("slope", 125.0)
        .hasFieldOrPropertyWithValue("differential", 14.4)
        .hasFieldOrPropertyWithValue("indexEstablished", true);
  }

  @Test
  void shouldFindAllForCurrentUser() {
    // Given: Multiple users with scorecards
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    scorecardRepository.save(createScorecard(88));
    scorecardRepository.save(createScorecard(90));

    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    scorecardRepository.save(createScorecard(85));

    // When: Gary searches for his scorecards
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    List<ScorecardEntity> garyResults = scorecardRepository.findAllForCurrentUser();

    // Then: Only Gary's scorecards should be returned
    assertThat(garyResults)
        .hasSize(2)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.GARY_GOLFER.sub()));

    // When: Pat searches for their scorecards
    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    List<ScorecardEntity> patResults = scorecardRepository.findAllForCurrentUser();

    // Then: Only Pat's scorecard should be returned
    assertThat(patResults).hasSize(1);
    assertThat(patResults.getFirst().getCreatedBy()).isEqualTo(JwtPersona.PAT_PUTTER.sub());
  }

  @Test
  void shouldFindByIdForCurrentUser() {
    // Given: Gary creates a scorecard
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    ScorecardEntity saved = scorecardRepository.save(createScorecard(88));

    // When: Gary looks up his own scorecard
    Optional<ScorecardEntity> garyResult =
        scorecardRepository.findByIdForCurrentUser(saved.getId());

    // Then: Scorecard should be found
    assertThat(garyResult).isPresent();
    assertThat(garyResult.get().getId()).isEqualTo(saved.getId());

    // When: Pat tries to access Gary's scorecard
    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    Optional<ScorecardEntity> patResult = scorecardRepository.findByIdForCurrentUser(saved.getId());

    // Then: Scorecard should not be found (authorization check)
    assertThat(patResult).isEmpty();
  }

  @Test
  void shouldDeleteByIdForCurrentUser() {
    // Given: Gary creates a scorecard
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    ScorecardEntity saved = scorecardRepository.save(createScorecard(88));

    // When: Pat tries to delete Gary's scorecard
    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    int patDeleteCount = scorecardRepository.deleteByIdForCurrentUser(saved.getId());

    // Then: Nothing should be deleted (authorization check)
    assertThat(patDeleteCount).isZero();

    // Verify scorecard still exists
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    assertThat(scorecardRepository.findByIdForCurrentUser(saved.getId())).isPresent();

    // When: Gary deletes his own scorecard
    int garyDeleteCount = scorecardRepository.deleteByIdForCurrentUser(saved.getId());

    // Then: Scorecard should be deleted
    assertThat(garyDeleteCount).isEqualTo(1);
    assertThat(scorecardRepository.findByIdForCurrentUser(saved.getId())).isEmpty();
  }

  @Test
  void findMostRecentForUser_isScopedToUser() {
    // Given: Gary and Pat each have a scorecard
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    scorecardRepository.save(createScorecard(88));

    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    scorecardRepository.save(createScorecard(85));

    // When: querying by Gary's user ID
    List<ScorecardEntity> results =
        scorecardRepository.findMostRecentForUser(
            JwtPersona.GARY_GOLFER.sub(), PageRequest.of(0, 20));

    // Then: only Gary's scorecard is returned
    assertThat(results)
        .hasSize(1)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.GARY_GOLFER.sub()));
  }

  @Test
  void findMostRecentForUser_returnsBoundedResultsOrderedByDateDesc() {
    // Given: Gary has 25 scorecards with dates spanning 25 consecutive days
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    LocalDate baseDate = LocalDate.of(2025, 1, 1);
    IntStream.range(0, 25)
        .forEach(i -> scorecardRepository.save(createScorecard(80, baseDate.plusDays(i))));

    // When: fetching the most recent 20
    List<ScorecardEntity> results =
        scorecardRepository.findMostRecentForUser(
            JwtPersona.GARY_GOLFER.sub(), PageRequest.of(0, 20));

    // Then: exactly 20 returned, ordered most-recent first (days 24..5, not days 4..0)
    assertThat(results).hasSize(20);
    assertThat(results.getFirst().getScoreDate()).isEqualTo(baseDate.plusDays(24));
    assertThat(results.getLast().getScoreDate()).isEqualTo(baseDate.plusDays(5));
  }

  @Test
  void findFirstPageForCurrentUser_isScopedToUserAndOrderedByDateDesc() {
    // Given: Gary has 3 scorecards on different dates; Pat has 1
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    scorecardRepository.save(createScorecard(88, LocalDate.of(2025, 1, 10)));
    scorecardRepository.save(createScorecard(90, LocalDate.of(2025, 1, 20)));
    scorecardRepository.save(createScorecard(85, LocalDate.of(2025, 1, 5)));

    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    scorecardRepository.save(createScorecard(80, LocalDate.of(2025, 1, 15)));

    // When: Gary fetches first page (limit=2)
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    List<ScorecardEntity> results = scorecardRepository.findFirstPageForCurrentUser(Limit.of(2));

    // Then: 2 most recent of Gary's scorecards, newest first, no Pat records
    assertThat(results)
        .hasSize(2)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.GARY_GOLFER.sub()));
    assertThat(results.get(0).getScoreDate()).isEqualTo(LocalDate.of(2025, 1, 20));
    assertThat(results.get(1).getScoreDate()).isEqualTo(LocalDate.of(2025, 1, 10));
  }

  @Test
  void findFirstPageForCurrentUser_limitPlusOne_detectsHasNext() {
    // Given: Gary has exactly 3 scorecards
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    scorecardRepository.save(createScorecard(88, LocalDate.of(2025, 1, 10)));
    scorecardRepository.save(createScorecard(90, LocalDate.of(2025, 1, 20)));
    scorecardRepository.save(createScorecard(85, LocalDate.of(2025, 1, 5)));

    // When: fetching limit+1=3 rows (limit=2)
    List<ScorecardEntity> results = scorecardRepository.findFirstPageForCurrentUser(Limit.of(3));

    // Then: all 3 returned, caller can infer hasNext=true (size > limit)
    assertThat(results).hasSize(3);
  }

  @Test
  void findNextPageForCurrentUser_returnsResultsBeforeCursorOrderedByDateDesc() {
    // Given: Gary has 3 scorecards on distinct dates
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    ScorecardEntity oldest =
        scorecardRepository.save(createScorecard(85, LocalDate.of(2025, 1, 5)));
    scorecardRepository.save(createScorecard(88, LocalDate.of(2025, 1, 10)));
    ScorecardEntity newest =
        scorecardRepository.save(createScorecard(90, LocalDate.of(2025, 1, 20)));

    // When: fetching the page after the newest scorecard (cursor = newest)
    List<ScorecardEntity> results =
        scorecardRepository.findNextPageForCurrentUser(
            newest.getScoreDate(), newest.getId(), Limit.of(10));

    // Then: only the two older scorecards are returned, newest-first
    assertThat(results).hasSize(2);
    assertThat(results.get(0).getScoreDate()).isEqualTo(LocalDate.of(2025, 1, 10));
    assertThat(results.get(1).getScoreDate()).isEqualTo(LocalDate.of(2025, 1, 5));
  }

  @Test
  void findNextPageForCurrentUser_isScopedToCurrentUser() {
    // Given: Gary and Pat each have scorecards
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    ScorecardEntity garyNewest =
        scorecardRepository.save(createScorecard(90, LocalDate.of(2025, 1, 20)));
    scorecardRepository.save(createScorecard(85, LocalDate.of(2025, 1, 10)));

    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    scorecardRepository.save(createScorecard(80, LocalDate.of(2025, 1, 5)));

    // When: Gary fetches next page after his newest record
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    List<ScorecardEntity> results =
        scorecardRepository.findNextPageForCurrentUser(
            garyNewest.getScoreDate(), garyNewest.getId(), Limit.of(10));

    // Then: only Gary's older record, no Pat records
    assertThat(results)
        .hasSize(1)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.GARY_GOLFER.sub()));
    assertThat(results.get(0).getScoreDate()).isEqualTo(LocalDate.of(2025, 1, 10));
  }

  @Test
  void findNextPageForCurrentUser_sameDayCursorTiebreaksById() {
    // Given: Gary has 3 scorecards on the same date — different IDs (UUIDv7 insertion order)
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    LocalDate sameDay = LocalDate.of(2025, 6, 15);
    ScorecardEntity first = scorecardRepository.save(createScorecard(85, sameDay));
    ScorecardEntity second = scorecardRepository.save(createScorecard(88, sameDay));
    ScorecardEntity third = scorecardRepository.save(createScorecard(90, sameDay));

    // When: fetching page after the "second" record (by ID order)
    // Keyset: (sameDay, second.id) — should return only first (smaller ID on same date)
    List<ScorecardEntity> results =
        scorecardRepository.findNextPageForCurrentUser(
            second.getScoreDate(), second.getId(), Limit.of(10));

    // Then: only the record with the smallest ID (first inserted) is after the cursor
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getId()).isEqualTo(first.getId());
  }

  private ScorecardEntity createScorecard(Integer score) {
    return createScorecard(score, LocalDate.of(2025, 9, 21));
  }

  private ScorecardEntity createScorecard(Integer score, LocalDate scoreDate) {
    return new ScorecardEntity(
        scoreDate,
        "Test Course",
        "Test Tee",
        score,
        72,
        72.1,
        125.0,
        ScorecardType.EIGHTEEN,
        14.4,
        true);
  }

  private void setSecurityContext(String username) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(username, null, List.of());
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
