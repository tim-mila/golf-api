package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import java.math.BigDecimal;
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
  JpaTeeServiceImpl.class,
  TeeMapper.class,
  JpaCourseServiceImpl.class,
  CourseMapper.class
})
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class JpaTeeServiceImplIT {

  private final TeeService teeService;
  private final CourseService courseService;

  @Autowired
  JpaTeeServiceImplIT(TeeService teeService, CourseService courseService) {
    this.teeService = teeService;
    this.courseService = courseService;
  }

  @Test
  void contextLoads() {
    assertThat(teeService).isNotNull();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void createAndGet() {
    CourseDto course =
        courseService.create(
            new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN));

    Optional<TeeDto> created =
        teeService.create(
            course.courseId(),
            new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")));

    assertThat(created)
        .isPresent()
        .get()
        .hasFieldOrProperty("teeId")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("courseId", course.courseId())
        .hasFieldOrPropertyWithValue("name", "Blue")
        .hasFieldOrPropertyWithValue("par", 72)
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("131.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("71.2"));

    Optional<TeeDto> read = teeService.get(created.orElseThrow().teeId());
    assertThat(read).isPresent().contains(created.orElseThrow());
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void createCourseNotFound() {
    Optional<TeeDto> result =
        teeService.create(
            UUID.randomUUID(),
            new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")));

    assertThat(result).isEmpty();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void patch() {
    CourseDto course =
        courseService.create(
            new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN));

    TeeDto created =
        teeService
            .create(
                course.courseId(),
                new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")))
            .orElseThrow();

    Optional<TeeDto> patched =
        teeService.patch(
            created.teeId(),
            new PatchTeeRequest(
                Optional.of("White"),
                Optional.empty(),
                Optional.of(new BigDecimal("125.0")),
                Optional.of(new BigDecimal("69.5"))));

    assertThat(patched)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("teeId", created.teeId())
        .hasFieldOrPropertyWithValue("courseId", course.courseId())
        .hasFieldOrPropertyWithValue("createdAt", created.createdAt())
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("name", "White")
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("125.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("69.5"));

    Optional<TeeDto> read = teeService.get(created.teeId());
    assertThat(read.orElseThrow().lastModifiedAt()).isAfter(created.lastModifiedAt());
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void listByCourse() {
    CourseDto course =
        courseService.create(
            new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN));

    teeService.create(
        course.courseId(),
        new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")));
    teeService.create(
        course.courseId(),
        new CreateTeeRequest("White", 72, new BigDecimal("125.0"), new BigDecimal("69.5")));

    List<TeeDto> tees = teeService.listByCourse(course.courseId());

    assertThat(tees).hasSize(2).extracting(TeeDto::name).containsExactlyInAnyOrder("Blue", "White");
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void deleteTest() {
    CourseDto course =
        courseService.create(
            new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN));

    TeeDto created =
        teeService
            .create(
                course.courseId(),
                new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")))
            .orElseThrow();

    teeService.delete(created.teeId());

    assertThat(teeService.get(created.teeId())).isEmpty();
  }
}
