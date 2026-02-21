package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseMapperTest {

  private final CourseMapper courseMapper = new CourseMapper();

  @Test
  void mapCreateCourseRequest() {
    CreateCourseRequest request =
        new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN);

    CourseEntity result = courseMapper.map(request);

    assertThat(result)
        .isEqualTo(new CourseEntity("Test Club", "Test Course", "Test City", USState.WISCONSIN));
  }

  @Test
  void mapCourseEntity() {
    UUID courseId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    CourseEntity entity = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    entity.setCourseId(courseId);
    entity.setCreatedAt(createdAt);

    CourseDto result = courseMapper.map(entity);

    assertThat(result)
        .isEqualTo(
            new CourseDto(
                courseId, createdAt, null, "Test Club", "Test Course", "Test City", USState.OHIO));
  }

  @Test
  void mapOptionalPresent() {
    UUID courseId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    CourseEntity entity =
        new CourseEntity("Test Club", "Test Course", "Test City", USState.WISCONSIN);
    entity.setCourseId(courseId);
    entity.setCreatedAt(createdAt);

    Optional<CourseDto> result = courseMapper.map(Optional.of(entity));

    assertThat(result)
        .isPresent()
        .contains(
            new CourseDto(
                courseId,
                createdAt,
                null,
                "Test Club",
                "Test Course",
                "Test City",
                USState.WISCONSIN));
  }

  @Test
  void mapOptionalEmpty() {
    Optional<CourseDto> result = courseMapper.map(Optional.empty());

    assertThat(result).isEmpty();
  }
}
