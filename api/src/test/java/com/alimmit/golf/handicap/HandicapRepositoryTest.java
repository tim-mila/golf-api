package com.alimmit.golf.handicap;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import com.alimmit.golf.utils.JwtPersona;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.history.Revisions;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaAuditingConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class HandicapRepositoryTest {

  private final HandicapRepository handicapRepository;

  @Autowired
  HandicapRepositoryTest(HandicapRepository handicapRepository) {
    this.handicapRepository = handicapRepository;
  }

  @Test
  void contextLoads() {
    Assertions.assertThat(handicapRepository).isNotNull();
  }

  @Test
  @WithMockUser(username = "123") // Gary Golfer's sub
  void shouldSaveHandicapWithAuditFields() {
    // Given: Gary Golfer is authenticated (based on @WithMockUser)

    // When: Creating a new handicap
    HandicapEntity entity = new HandicapEntity("123", 15.1, 5, 5);
    HandicapEntity saved = handicapRepository.save(entity);

    // Then: Audit fields should be populated automatically
    assertThat(saved)
        .hasFieldOrProperty("id")
        .hasFieldOrPropertyWithValue("golferId", JwtPersona.GARY_GOLFER.sub())
        .hasFieldOrProperty("createdAt")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("handicapIndex", 15.1)
        .hasFieldOrPropertyWithValue("roundsUsed", 5)
        .hasFieldOrPropertyWithValue("totalRounds", 5);
  }

  @Test
  void shouldFindAllForCurrentUser() {
    // Given: Multiple users with handicaps
    handicapRepository.save(createHandicap(JwtPersona.GARY_GOLFER.sub(), 15.1, 5, 5));
    handicapRepository.save(createHandicap(JwtPersona.GARY_GOLFER.sub(), 15.0, 6, 6));
    handicapRepository.save(createHandicap(JwtPersona.PAT_PUTTER.sub(), 5.6, 8, 20));

    // When: Gary searches for his scorecards
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    Optional<HandicapEntity> garyResults = handicapRepository.findHandicapForCurrentUser();

    // Then: Only Gary's scorecards should be returned
    assertThat(garyResults)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("golferId", JwtPersona.GARY_GOLFER.sub())
        .hasFieldOrPropertyWithValue("handicapIndex", 15.0)
        .hasFieldOrPropertyWithValue("roundsUsed", 6)
        .hasFieldOrPropertyWithValue("totalRounds", 6);

    // When: Pat searches for their scorecards
    setSecurityContext(JwtPersona.PAT_PUTTER.sub());
    Optional<HandicapEntity> patResults = handicapRepository.findHandicapForCurrentUser();

    // Then: Only Pat's scorecard should be returned
    assertThat(patResults)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("golferId", JwtPersona.PAT_PUTTER.sub())
        .hasFieldOrPropertyWithValue("handicapIndex", 5.6)
        .hasFieldOrPropertyWithValue("roundsUsed", 8)
        .hasFieldOrPropertyWithValue("totalRounds", 20);
  }

  @Test
  @Transactional(propagation = Propagation.NEVER)
  void testGetRevisions() {

    // Given: Multiple handicaps for Gary Golfer
    handicapRepository.save(createHandicap(JwtPersona.GARY_GOLFER.sub(), 15.1, 5, 5));
    handicapRepository.save(createHandicap(JwtPersona.GARY_GOLFER.sub(), 15.0, 6, 6));
    HandicapEntity handicap =
        handicapRepository.save(createHandicap(JwtPersona.GARY_GOLFER.sub(), 14.3, 7, 7));

    // WHEN: fetching handicap revision history
    setSecurityContext(JwtPersona.GARY_GOLFER.sub());
    Revisions<Integer, HandicapEntity> revisions =
        handicapRepository.findRevisions(handicap.getId());

    // THEN: the history contains three handicaps
    assertThat(revisions).hasSize(3);
  }

  private HandicapEntity createHandicap(
      String golferId, Double index, Integer roundsUsed, Integer totalRounds) {

    return handicapRepository
        .findHandicapForUser(golferId)
        .map(
            h -> {
              h.setHandicapIndex(index);
              h.setTotalRounds(totalRounds);
              h.setRoundsUsed(roundsUsed);
              return h;
            })
        .orElse(new HandicapEntity(golferId, index, roundsUsed, totalRounds));
  }

  private void setSecurityContext(String username) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(username, null, List.of());
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
