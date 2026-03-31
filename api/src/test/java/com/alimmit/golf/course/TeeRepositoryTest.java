package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;

import com.alimmit.golf.TestJpaAuditingConfig;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
@Import(TestJpaAuditingConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeeRepositoryTest {

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
            new TeeEntity(course, "Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")));

    assertThat(saved)
        .hasFieldOrProperty("id")
        .hasFieldOrProperty("createdAt")
        .hasFieldOrPropertyWithValue("createdBy", "123")
        .hasFieldOrProperty("lastModifiedAt")
        .hasFieldOrPropertyWithValue("lastModifiedBy", "123")
        .hasFieldOrPropertyWithValue("name", "Blue")
        .hasFieldOrPropertyWithValue("par", 72)
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("131.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("71.2"));

    Optional<TeeEntity> read = teeRepository.findById(saved.getId());
    assertThat(read)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("id", saved.getId())
        .hasFieldOrPropertyWithValue("name", "Blue");

    TeeEntity toUpdate = read.get();
    toUpdate.setName("White");
    toUpdate.setSlope(new BigDecimal("125.0"));
    toUpdate.setRating(new BigDecimal("69.5"));

    TeeEntity updated = teeRepository.save(toUpdate);
    assertThat(updated)
        .hasFieldOrPropertyWithValue("id", saved.getId())
        .hasFieldOrPropertyWithValue("name", "White")
        .hasFieldOrPropertyWithValue("slope", new BigDecimal("125.0"))
        .hasFieldOrPropertyWithValue("rating", new BigDecimal("69.5"));

    assertThat(updated.getLastModifiedAt()).isAfter(saved.getLastModifiedAt());

    teeRepository.delete(updated);

    assertThat(teeRepository.findById(saved.getId())).isEmpty();
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void findByCourseId() {
    CourseEntity course1 =
        courseRepository.save(new CourseEntity("Club A", "Course A", "City A", USState.WISCONSIN));
    CourseEntity course2 =
        courseRepository.save(new CourseEntity("Club B", "Course B", "City B", USState.OHIO));

    teeRepository.save(
        new TeeEntity(course1, "Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2")));

    teeRepository.save(
        new TeeEntity(course1, "White", 72, new BigDecimal("125.0"), new BigDecimal("69.5")));
    teeRepository.save(
        new TeeEntity(course2, "Red", 72, new BigDecimal("118.0"), new BigDecimal("66.0")));

    List<TeeEntity> course1Tees = teeRepository.findByCourseIdForCurrentUser(course1.getId());

    assertThat(course1Tees)
        .hasSize(2)
        .extracting(TeeEntity::getName)
        .containsExactlyInAnyOrder("Blue", "White");

    List<TeeEntity> course2Tees = teeRepository.findByCourseIdForCurrentUser(course2.getId());

    assertThat(course2Tees).hasSize(1).extracting(TeeEntity::getName).containsExactly("Red");
  }

  @Test
  @WithMockUser("123")
  @Transactional(propagation = Propagation.NEVER)
  void findByCourseIdEmpty() {
    List<TeeEntity> result =
        teeRepository.findByCourseIdForCurrentUser(java.util.UUID.randomUUID());

    assertThat(result).isEmpty();
  }
}
