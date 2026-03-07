package com.alimmit.golf.course;

import com.alimmit.golf.errors.DuplicateCourseException;
import com.alimmit.golf.errors.NotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class JpaCourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final TeeRepository teeRepository;

  JpaCourseServiceImpl(
      CourseRepository courseRepository, CourseMapper courseMapper, TeeRepository teeRepository) {
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.teeRepository = teeRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CourseDto> list() {
    return courseRepository.findAllForCurrentUser().stream().map(courseMapper::map).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CourseDto> get(UUID courseId) {
    return courseMapper.map(courseRepository.findByIdForCurrentUser(courseId));
  }

  @Override
  @Transactional
  public CourseDto create(CreateCourseRequest request) {
    if (courseRepository.existsByUniqueConstraintForCurrentUser(
        request.club(), request.course(), request.city(), request.state())) {
      throw new DuplicateCourseException();
    }
    try {
      return courseMapper.map(courseRepository.save(courseMapper.map(request)));
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateCourseException();
    }
  }

  @Override
  @Transactional
  public Optional<CourseDto> patch(UUID courseId, PatchCourseRequest request) {
    return courseRepository
        .findByIdForCurrentUser(courseId)
        .map(
            c -> {
              String mergedClub = request.club().orElse(c.getClub());
              String mergedCourse = request.course().orElse(c.getCourse());
              String mergedCity = request.city().orElse(c.getCity());
              USState mergedState = request.state().orElse(c.getState());
              if (courseRepository.existsByUniqueConstraintForCurrentUserExcluding(
                  mergedClub, mergedCourse, mergedCity, mergedState, courseId)) {
                throw new DuplicateCourseException();
              }
              c.setClub(mergedClub);
              c.setCourse(mergedCourse);
              c.setCity(mergedCity);
              c.setState(mergedState);
              return c;
            })
        .map(
            c -> {
              try {
                return courseMapper.map(courseRepository.save(c));
              } catch (DataIntegrityViolationException e) {
                throw new DuplicateCourseException();
              }
            });
  }

  @Override
  @Transactional
  public void delete(UUID courseId) {
    courseRepository.findByIdForCurrentUser(courseId).orElseThrow(NotFoundException::new);
    teeRepository.deleteByCourse_Id(courseId);
    courseRepository.deleteById(courseId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CourseDto> search(String query) {
    String sanitized = query.trim().replaceAll("[^\\w\\s]", "").trim();
    if (sanitized.isBlank()) {
      return List.of();
    }
    String tsQuery =
        Arrays.stream(sanitized.split("\\s+"))
            .map(word -> word + ":*")
            .collect(Collectors.joining(" & "));
    return courseRepository.search(tsQuery).stream().map(courseMapper::map).toList();
  }
}
