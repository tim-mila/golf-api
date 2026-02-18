package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TeeRepositoryTest.TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeeRepositoryTest {

  @TestConfiguration
  @EnableJpaAuditing
  static class TestConfig {

    @Bean
    AuditorAware<String> auditorAware() {
      return () -> {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
          return Optional.empty();
        }
        return Optional.of(authentication.getName());
      };
    }

    @Bean
    SecurityEvaluationContextExtension securityEvaluationContextExtension() {
      return new SecurityEvaluationContextExtension();
    }
  }

  private final TeeRepository teeRepository;
  private final CourseRepository courseRepository;

  @Autowired
  TeeRepositoryTest(TeeRepository teeRepository, CourseRepository courseRepository) {
    this.teeRepository = teeRepository;
    this.courseRepository = courseRepository;
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void crudTest() {
    CourseEntity course =
        courseRepository.save(
            new CourseEntity("Test Club", "Test Course", "Test City", USState.WISCONSIN));

    TeeEntity saved =
        teeRepository.save(
            new TeeEntity(
                course, "Blue", 6500, new BigDecimal("131.0"), new BigDecimal("71.2")));

    assertThat(saved)
        .hasFieldOrProperty("teeId")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("createdBy", "123")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("lastModifiedBy", "123")
        .hasFieldOrPropertyWithValue("name", "Blue")
        .hasFieldOrPropertyWithValue("yardage", 6500)
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("131.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("71.2"));

    Optional<TeeEntity> read = teeRepository.findById(saved.getTeeId());
    assertThat(read)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("teeId", saved.getTeeId())
        .hasFieldOrPropertyWithValue("name", "Blue")
        .hasFieldOrPropertyWithValue("yardage", 6500);

    TeeEntity toUpdate = read.get();
    toUpdate.setName("White");
    toUpdate.setYardage(6100);
    toUpdate.setSlope(new BigDecimal("125.0"));
    toUpdate.setRating(new BigDecimal("69.5"));

    TeeEntity updated = teeRepository.save(toUpdate);
    assertThat(updated)
        .hasFieldOrPropertyWithValue("teeId", saved.getTeeId())
        .hasFieldOrPropertyWithValue("name", "White")
        .hasFieldOrPropertyWithValue("yardage", 6100)
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("125.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("69.5"));

    assertThat(updated.getLastModifiedAt()).isAfter(saved.getLastModifiedAt());

    teeRepository.delete(updated);

    assertThat(teeRepository.findById(saved.getTeeId())).isEmpty();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void findByCourseId() {
    CourseEntity course1 =
        courseRepository.save(
            new CourseEntity("Club A", "Course A", "City A", USState.WISCONSIN));
    CourseEntity course2 =
        courseRepository.save(
            new CourseEntity("Club B", "Course B", "City B", USState.OHIO));

    TeeEntity tee1 =
        teeRepository.save(
            new TeeEntity(
                course1, "Blue", 6500, new BigDecimal("131.0"), new BigDecimal("71.2")));
    TeeEntity tee2 =
        teeRepository.save(
            new TeeEntity(
                course1, "White", 6100, new BigDecimal("125.0"), new BigDecimal("69.5")));
    teeRepository.save(
        new TeeEntity(
            course2, "Red", 5200, new BigDecimal("118.0"), new BigDecimal("66.0")));

    List<TeeEntity> course1Tees = teeRepository.findByCourse_CourseId(course1.getCourseId());

    assertThat(course1Tees)
        .hasSize(2)
        .extracting(TeeEntity::getName)
        .containsExactlyInAnyOrder("Blue", "White");

    List<TeeEntity> course2Tees = teeRepository.findByCourse_CourseId(course2.getCourseId());

    assertThat(course2Tees)
        .hasSize(1)
        .extracting(TeeEntity::getName)
        .containsExactly("Red");
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void findByCourseIdEmpty() {
    List<TeeEntity> result = teeRepository.findByCourse_CourseId(java.util.UUID.randomUUID());

    assertThat(result).isEmpty();
  }
}
