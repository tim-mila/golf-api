package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alimmit.golf.errors.NotFoundException;
import java.math.BigDecimal;
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
class JpaTeeServiceImplTest {

  @Mock private TeeRepository teeRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private TeeMapper teeMapper;

  private JpaTeeServiceImpl teeService;

  @BeforeEach
  void setUp() {
    teeService = new JpaTeeServiceImpl(teeRepository, courseRepository, teeMapper);
  }

  @Test
  void listByCourse() {
    UUID courseId = UUID.randomUUID();
    TeeEntity entity1 = createEntity(UUID.randomUUID(), "Blue");
    TeeEntity entity2 = createEntity(UUID.randomUUID(), "White");
    TeeDto dto1 = createDto(entity1);
    TeeDto dto2 = createDto(entity2);

    when(teeRepository.findByCourseIdForCurrentUser(courseId))
        .thenReturn(List.of(entity1, entity2));
    when(teeMapper.map(entity1)).thenReturn(dto1);
    when(teeMapper.map(entity2)).thenReturn(dto2);

    List<TeeDto> result = teeService.listByCourse(courseId);

    assertThat(result).containsExactly(dto1, dto2);
  }

  @Test
  void get() {
    UUID teeId = UUID.randomUUID();
    TeeEntity entity = createEntity(teeId, "Blue");
    TeeDto dto = createDto(entity);
    Optional<TeeEntity> optionalEntity = Optional.of(entity);

    when(teeRepository.findByIdForCurrentUser(teeId)).thenReturn(optionalEntity);
    when(teeMapper.map(optionalEntity)).thenReturn(Optional.of(dto));

    Optional<TeeDto> result = teeService.get(teeId);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void getNotFound() {
    UUID teeId = UUID.randomUUID();
    Optional<TeeEntity> empty = Optional.empty();

    when(teeRepository.findByIdForCurrentUser(teeId)).thenReturn(empty);
    when(teeMapper.map(empty)).thenReturn(Optional.empty());

    Optional<TeeDto> result = teeService.get(teeId);

    assertThat(result).isEmpty();
  }

  @Test
  void create() {
    UUID courseId = UUID.randomUUID();
    CourseEntity course = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    course.setId(courseId);

    CreateTeeRequest request =
        new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2"));
    TeeEntity unsaved =
        new TeeEntity(course, "Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2"));
    TeeEntity saved = createEntity(UUID.randomUUID(), "Blue");
    TeeDto dto = createDto(saved);

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.of(course));
    when(teeMapper.map(course, request)).thenReturn(unsaved);
    when(teeRepository.save(unsaved)).thenReturn(saved);
    when(teeMapper.map(saved)).thenReturn(dto);

    Optional<TeeDto> result = teeService.create(courseId, request);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void createCourseNotFound() {
    UUID courseId = UUID.randomUUID();
    CreateTeeRequest request =
        new CreateTeeRequest("Blue", 72, new BigDecimal("131.0"), new BigDecimal("71.2"));

    when(courseRepository.findByIdForCurrentUser(courseId)).thenReturn(Optional.empty());

    Optional<TeeDto> result = teeService.create(courseId, request);

    assertThat(result).isEmpty();
  }

  @Test
  void patch() {
    UUID teeId = UUID.randomUUID();
    TeeEntity entity = createEntity(teeId, "Blue");
    TeeEntity savedEntity = createEntity(teeId, "White");
    TeeDto dto = createDto(savedEntity);

    PatchTeeRequest request =
        new PatchTeeRequest(
            Optional.of("White"), Optional.empty(), Optional.empty(), Optional.empty());

    when(teeRepository.findByIdForCurrentUser(teeId)).thenReturn(Optional.of(entity));
    when(teeRepository.save(entity)).thenReturn(savedEntity);
    when(teeMapper.map(savedEntity)).thenReturn(dto);

    Optional<TeeDto> result = teeService.patch(teeId, request);

    assertThat(result).isPresent().contains(dto);
  }

  @Test
  void patchNotFound() {
    UUID teeId = UUID.randomUUID();
    PatchTeeRequest request =
        new PatchTeeRequest(
            Optional.of("White"), Optional.empty(), Optional.empty(), Optional.empty());

    when(teeRepository.findByIdForCurrentUser(teeId)).thenReturn(Optional.empty());

    Optional<TeeDto> result = teeService.patch(teeId, request);

    assertThat(result).isEmpty();
  }

  @Test
  void delete() {
    UUID teeId = UUID.randomUUID();

    when(teeRepository.deleteByIdForCurrentUser(teeId)).thenReturn(1);

    teeService.delete(teeId);

    verify(teeRepository).deleteByIdForCurrentUser(teeId);
  }

  @Test
  void deleteNotFound() {
    UUID teeId = UUID.randomUUID();

    when(teeRepository.deleteByIdForCurrentUser(teeId)).thenReturn(0);

    assertThatThrownBy(() -> teeService.delete(teeId)).isInstanceOf(NotFoundException.class);
  }

  private TeeEntity createEntity(UUID teeId, String name) {
    CourseEntity course = new CourseEntity("Test Club", "Test Course", "Test City", USState.OHIO);
    course.setId(UUID.randomUUID());

    TeeEntity entity =
        new TeeEntity(course, name, 72, new BigDecimal("131.0"), new BigDecimal("71.2"));
    entity.setId(teeId);
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  private TeeDto createDto(TeeEntity entity) {
    return new TeeDto(
        entity.getId(),
        entity.getCourse().getId(),
        entity.getCreatedAt(),
        Instant.now(),
        entity.getName(),
        entity.getPar(),
        entity.getSlope(),
        entity.getRating());
  }
}
