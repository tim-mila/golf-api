package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeeMapperTest {

  private final TeeMapper teeMapper = new TeeMapper();

  @Test
  void mapCreateTeeRequest() {
    CourseEntity course = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    CreateTeeRequest request =
        new CreateTeeRequest("Blue", 72, 6500, new BigDecimal("131.0"), new BigDecimal("71.2"));

    TeeEntity result = teeMapper.map(course, request);

    assertThat(result)
        .isEqualTo(
            new TeeEntity(
                course, "Blue", 72, 6500, new BigDecimal("131.0"), new BigDecimal("71.2")));
  }

  @Test
  void mapTeeEntity() {
    UUID teeId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    CourseEntity course = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    course.setId(courseId);

    TeeEntity entity =
        new TeeEntity(course, "White", 72, 6100, new BigDecimal("125.0"), new BigDecimal("69.5"));
    entity.setId(teeId);
    entity.setCreatedAt(createdAt);

    TeeDto result = teeMapper.map(entity);

    assertThat(result)
        .isEqualTo(
            new TeeDto(
                teeId,
                courseId,
                createdAt,
                null,
                "White",
                72,
                6100,
                new BigDecimal("125.0"),
                new BigDecimal("69.5")));
  }

  @Test
  void mapOptionalPresent() {
    UUID teeId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    CourseEntity course = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    course.setId(courseId);

    TeeEntity entity =
        new TeeEntity(course, "Blue", 72, 6500, new BigDecimal("131.0"), new BigDecimal("71.2"));
    entity.setId(teeId);
    entity.setCreatedAt(createdAt);

    Optional<TeeDto> result = teeMapper.map(Optional.of(entity));

    assertThat(result)
        .isPresent()
        .contains(
            new TeeDto(
                teeId,
                courseId,
                createdAt,
                null,
                "Blue",
                72,
                6500,
                new BigDecimal("131.0"),
                new BigDecimal("71.2")));
  }

  @Test
  void mapOptionalEmpty() {
    Optional<TeeDto> result = teeMapper.map(Optional.empty());

    assertThat(result).isEmpty();
  }
}
