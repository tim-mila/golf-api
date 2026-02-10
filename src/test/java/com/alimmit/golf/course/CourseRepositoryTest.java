package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Disabled;
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
@Import(CourseRepositoryTest.TestConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Disabled
class CourseRepositoryTest {

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

  private final CourseRepository courseRepository;

  @Autowired
  CourseRepositoryTest(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void crudTest() {

    CourseEntity saved =
        courseRepository.save(
            new CourseEntity("Test Club", "Test Course", "Somewhere", USState.WISCONSIN));
    assertThat(saved)
        .hasFieldOrProperty("courseId")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("createdBy", "123")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("lastModifiedBy", "123")
        .hasFieldOrPropertyWithValue("club", "Test Club")
        .hasFieldOrPropertyWithValue("course", "Test Course")
        .hasFieldOrPropertyWithValue("city", "Somewhere")
        .hasFieldOrPropertyWithValue("state", USState.WISCONSIN);

    Optional<CourseEntity> read = courseRepository.findById(saved.getCourseId());
    assertThat(read)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("courseId", saved.getCourseId())
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("createdBy", "123")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("lastModifiedBy", "123")
        .hasFieldOrPropertyWithValue("club", "Test Club")
        .hasFieldOrPropertyWithValue("course", "Test Course")
        .hasFieldOrPropertyWithValue("city", "Somewhere")
        .hasFieldOrPropertyWithValue("state", USState.WISCONSIN);

    CourseEntity toUpdate = read.get();
    toUpdate.setCity("Other place");
    toUpdate.setState(USState.ALASKA);
    toUpdate.setClub("New Test Club");
    toUpdate.setCourse("New Test Course");

    CourseEntity updated = courseRepository.save(toUpdate);
    assertThat(updated)
        .hasFieldOrPropertyWithValue("courseId", saved.getCourseId())
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("createdBy", "123")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("lastModifiedBy", "123")
        .hasFieldOrPropertyWithValue("club", "New Test Club")
        .hasFieldOrPropertyWithValue("course", "New Test Course")
        .hasFieldOrPropertyWithValue("city", "Other place")
        .hasFieldOrPropertyWithValue("state", USState.ALASKA);

    assertThat(updated.getLastModifiedAt()).isAfter(saved.getLastModifiedAt());

    courseRepository.delete(updated);

    assertThat(courseRepository.findById(saved.getCourseId())).isEmpty();
  }
}
