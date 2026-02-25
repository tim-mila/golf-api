package com.alimmit.golf.course;

import com.alimmit.golf.errors.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    return courseMapper.map(courseRepository.save(courseMapper.map(request)));
  }

  @Override
  @Transactional
  public Optional<CourseDto> patch(UUID courseId, PatchCourseRequest request) {

    Optional<CourseEntity> toUpdate =
        courseRepository
            .findByIdForCurrentUser(courseId)
            .map(
                c -> {
                  c.setClub(request.club().orElse(c.getClub()));
                  c.setCourse(request.course().orElse(c.getCourse()));
                  c.setCity(request.city().orElse(c.getCity()));
                  c.setState(request.state().orElse(c.getState()));
                  return c;
                });
    return toUpdate.map(c -> courseMapper.map(courseRepository.save(c)));
  }

  @Override
  @Transactional
  public void delete(UUID courseId) {
    courseRepository.findByIdForCurrentUser(courseId).orElseThrow(NotFoundException::new);
    teeRepository.deleteByCourse_Id(courseId);
    courseRepository.deleteById(courseId);
  }
}
