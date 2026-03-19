package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import com.alimmit.golf.utils.JwtPersona;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
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
  void shouldFindAllCreatedBy() {
    // Given: Gary creates 2 scorecards, Pat creates 1
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    scorecardRepository.save(createScorecard(88));
    scorecardRepository.save(createScorecard(90));

    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    scorecardRepository.save(createScorecard(85));

    // When: Querying by Gary's user ID directly
    List<ScorecardEntity> garyResults =
        scorecardRepository.findAllCreatedBy(JwtPersona.GARY_GOLFER.sub());

    // Then: Only Gary's scorecards are returned
    assertThat(garyResults)
        .hasSize(2)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.GARY_GOLFER.sub()));

    // When: Querying by Pat's user ID directly
    List<ScorecardEntity> patResults =
        scorecardRepository.findAllCreatedBy(JwtPersona.PAT_PUTTER.sub());

    // Then: Only Pat's scorecard is returned
    assertThat(patResults)
        .hasSize(1)
        .allMatch(s -> s.getCreatedBy().equals(JwtPersona.PAT_PUTTER.sub()));
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

  private ScorecardEntity createScorecard(Integer score) {
    return new ScorecardEntity(
        LocalDate.of(2025, 9, 21),
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
