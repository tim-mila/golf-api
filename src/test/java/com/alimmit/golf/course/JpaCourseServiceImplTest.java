package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    when(courseRepository.findAll()).thenReturn(List.of(entity1, entity2));
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

    when(courseRepository.findById(courseId)).thenReturn(optionalEntity);
    when(courseMapper.map(optionalEntity)).thenReturn(Optional.of(dto));

    Optional<CourseDto> result = courseService.get(courseId);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void getNotFound() {
    UUID courseId = UUID.randomUUID();
    Optional<CourseEntity> empty = Optional.empty();

    when(courseRepository.findById(courseId)).thenReturn(empty);
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
  void patch() {
    UUID courseId = UUID.randomUUID();
    CourseEntity entity = createEntity(courseId, "Old Club", "Old Course");
    CourseEntity savedEntity = createEntity(courseId, "New Club", "New Course");
    CourseDto dto = createDto(savedEntity);

    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("New Club"), Optional.of("New Course"), Optional.empty(), Optional.empty());

    when(courseRepository.findById(courseId)).thenReturn(Optional.of(entity));
    when(courseRepository.save(entity)).thenReturn(savedEntity);
    when(courseMapper.map(savedEntity)).thenReturn(dto);

    Optional<CourseDto> result = courseService.patch(courseId, request);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void patchNotFound() {
    UUID courseId = UUID.randomUUID();
    PatchCourseRequest request =
        new PatchCourseRequest(
            Optional.of("New Club"), Optional.empty(), Optional.empty(), Optional.empty());

    when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

    Optional<CourseDto> result = courseService.patch(courseId, request);

    assertThat(result).isEmpty();
  }

  @Test
  void delete() {
    UUID courseId = UUID.randomUUID();

    courseService.delete(courseId);

    verify(teeRepository).deleteByCourse_CourseId(courseId);
    verify(courseRepository).deleteById(courseId);
  }

  private CourseEntity createEntity(UUID courseId, String club, String course) {
    CourseEntity entity = new CourseEntity(club, course, "Test City", USState.WISCONSIN);
    entity.setCourseId(courseId);
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  private CourseDto createDto(CourseEntity entity) {
    return new CourseDto(
        entity.getCourseId(),
        entity.getCreatedAt(),
        Instant.now(),
        entity.getClub(),
        entity.getCourse(),
        entity.getCity(),
        entity.getState());
  }
}
