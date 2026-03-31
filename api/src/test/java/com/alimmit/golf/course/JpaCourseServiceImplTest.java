package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alimmit.golf.errors.DuplicateException;
import com.alimmit.golf.errors.NotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class JpaCourseServiceImplTest {

  @Mock private CourseRepository courseRepository;
  @Mock private CourseMapper courseMapper;
  @Mock private TeeRepository teeRepository;

  private JpaCourseServiceImpl courseService;

  @BeforeEach
  void setUp() {
    courseService = new JpaCourseServiceImpl(courseRepository, courseMapper, teeRepository);
  }

  @Test
  void list() {
    CourseEntity entity1 = createEntity(UUID.randomUUID(), "Club A", "Course A");
    CourseEntity entity2 = createEntity(UUID.randomUUID(), "Club B", "Course B");
    CourseDto dto1 = createDto(entity1);
    CourseDto dto2 = createDto(entity2);

    when(courseRepository.findAllForCurrentUser()).thenReturn(List.of(entity1, entity2));
    when(courseMapper.map(entity1)).thenReturn(dto1);
    when(courseMapper.map(entity2)).thenReturn(dto2);

    List<CourseDto> result = courseService.list();

    assertThat(result).containsExactly(dto1, dto2);
  }

  @Test
  void get() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Test Club", "Test Course");
    CourseDto dto = createDto(entity);
    Optional<CourseEntity> optionalEntity = Optional.of(entity);

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(optionalEntity);
    when(courseMapper.map(optionalEntity)).thenReturn(Optional.of(dto));

    Optional<CourseDto> result = courseService.get(courseId);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void getNotFound() {
    UUID courseId = UUID.randomUUID();
    Optional<CourseEntity> empty = Optional.empty();

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(empty);
    when(courseMapper.map(empty)).thenReturn(Optional.empty());

    Optional<CourseDto> result = courseService.get(courseId);

    assertThat(result).isEmpty();
  }

  @Test
  void create() {
    CreateCourseRequest request =
        new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN);
    CourseEntity unsaved =
        new CourseEntity("Test Club", "Test Course", "Test City", USState.WISCONSIN);
    CourseEntity saved = createEntity(UUID.randomUUID(), "Test Club", "Test Course");
    CourseDto dto = createDto(saved);

    when(courseMapper.map(request)).thenReturn(unsaved);
    when(courseRepository.save(unsaved)).thenReturn(saved);
    when(courseMapper.map(saved)).thenReturn(dto);

    CourseDto result = courseService.create(request);

    assertThat(result).isEqualTo(dto);
  }

  @Test
  void createDuplicate_ExpectDuplicateException() {
    CreateCourseRequest request =
        new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN);

    when(courseRepository.existsByUniqueConstraintForCurrentUser(
            "Test Club", "Test Course", "Test City", USState.WISCONSIN))
        .thenReturn(true);

    assertThatThrownBy(() -> courseService.create(request)).isInstanceOf(DuplicateException.class);
  }

  @Test
  void patch() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Old Club", "Old Course");
    CourseEntity savedEntity = createEntity(courseId, "New Club", "New Course");
    CourseDto dto = createDto(savedEntity);

    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("New Club"), Optional.of("New Course"), Optional.empty(), Optional.empty());

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.of(entity));
    when(courseRepository.save(entity)).thenReturn(savedEntity);
    when(courseMapper.map(savedEntity)).thenReturn(dto);

    Optional<CourseDto> result = courseService.patch(courseId, request);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void patchDuplicate_ExpectDuplicateException() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Old Club", "Old Course");
    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("Other Club"), Optional.empty(), Optional.empty(), Optional.empty());

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.of(entity));
    when(courseRepository.existsByUniqueConstraintForCurrentUserExcluding(
            "Other Club", "Old Course", "Test City", USState.WISCONSIN, courseId))
        .thenReturn(true);

    assertThatThrownBy(() -> courseService.patch(courseId, request))
        .isInstanceOf(DuplicateException.class);
  }

  @Test
  void patchNotFound() {
    UUID courseId = UUID.randomUUID();
    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("New Club"), Optional.empty(), Optional.empty(), Optional.empty());

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.empty());

    Optional<CourseDto> result = courseService.patch(courseId, request);

    assertThat(result).isEmpty();
  }

  @Test
  void delete() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Test Club", "Test Course");

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.of(entity));

    courseService.delete(courseId);

    verify(teeRepository).deleteByCourse_Id(courseId);
    verify(courseRepository).deleteById(courseId);
  }

  @Test
  void deleteNotFound() {
    UUID courseId = UUID.randomUUID();

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.delete(courseId)).isInstanceOf(NotFoundException.class);
  }

  // --- Gap 3: TOCTOU DataIntegrityViolationException fallback ---

  @Test
  void create_whenSaveThrowsDataIntegrityViolation_throwsDuplicateException() {
    CreateCourseRequest request =
        new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN);
    CourseEntity unsaved =
        new CourseEntity("Test Club", "Test Course", "Test City", USState.WISCONSIN);

    when(courseRepository.existsByUniqueConstraintForCurrentUser(
            "Test Club", "Test Course", "Test City", USState.WISCONSIN))
        .thenReturn(false);
    when(courseMapper.map(request)).thenReturn(unsaved);
    when(courseRepository.save(unsaved)).thenThrow(DataIntegrityViolationException.class);

    assertThatThrownBy(() -> courseService.create(request)).isInstanceOf(DuplicateException.class);
  }

  @Test
  void patch_whenSaveThrowsDataIntegrityViolation_throwsDuplicateException() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Old Club", "Old Course");
    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("New Club"), Optional.empty(), Optional.empty(), Optional.empty());

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.of(entity));
    when(courseRepository.existsByUniqueConstraintForCurrentUserExcluding(
            "New Club", "Old Course", "Test City", USState.WISCONSIN, courseId))
        .thenReturn(false);
    when(courseRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

    assertThatThrownBy(() -> courseService.patch(courseId, request))
        .isInstanceOf(DuplicateException.class);
  }

  // --- Gap 2: Search sanitization unit tests ---

  @Test
  void search_specialCharsOnly_returnsEmptyList() {
    List<CourseDto> result = courseService.search("!@#$%");

    assertThat(result).isEmpty();
  }

  @Test
  void search_multiWordQuery_buildsCorrectTsQuery() {
    when(courseRepository.search("Augusta:* & National:*")).thenReturn(List.of());

    courseService.search("Augusta National");

    verify(courseRepository).search("Augusta:* & National:*");
  }

  private CourseEntity createEntity(UUID courseId, String club, String course) {
    CourseEntity entity = new CourseEntity(club, course, "Test City", USState.WISCONSIN);
    entity.setId(courseId);
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  private CourseDto createDto(CourseEntity entity) {
    return new CourseDto(
        entity.getId(),
        entity.getCreatedAt(),
        Instant.now(),
        entity.getClub(),
        entity.getCourse(),
        entity.getCity(),
        entity.getState());
  }
}
