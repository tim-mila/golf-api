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
@Import({JpaCourseServiceImplTest.TestConfig.class, JpaCourseServiceImpl.class, CourseMapper.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Disabled
class JpaCourseServiceImplTest {

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

  private final CourseService courseService;

  @Autowired
  JpaCourseServiceImplTest(CourseService courseService) {
    this.courseService = courseService;
  }

  @Test
  void contextLoads() {
    assertThat(courseService).isNotNull();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void patch() {

    CourseDto created =
        courseService.create(
            new CreateCourseRequest(
                "Before Club", "Before Course", "Before City", USState.WISCONSIN));

    assertThat(created)
        .hasFieldOrProperty("courseId")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("club", "Before Club")
        .hasFieldOrPropertyWithValue("course", "Before Course")
        .hasFieldOrPropertyWithValue("city", "Before City")
        .hasFieldOrPropertyWithValue("state", USState.WISCONSIN);

    Optional<CourseDto> patched =
        courseService.patch(
            created.courseId(),
            new PatchCourseRequest(
                Optional.of("After Club"),
                Optional.of("After Course"),
                Optional.of("After City"),
                Optional.of(USState.RHODE_ISLAND)));

    assertThat(patched)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("courseId", created.courseId())
        .hasFieldOrPropertyWithValue("createdAt", created.createdAt())
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("club", "After Club")
        .hasFieldOrPropertyWithValue("course", "After Course")
        .hasFieldOrPropertyWithValue("city", "After City")
        .hasFieldOrPropertyWithValue("state", USState.RHODE_ISLAND);

    Optional<CourseDto> read = courseService.get(created.courseId());
    assertThat(read.orElseThrow().lastModifiedAt()).isAfter(created.lastModifiedAt());
  }
}
