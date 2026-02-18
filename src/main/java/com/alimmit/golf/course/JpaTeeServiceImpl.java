package com.alimmit.golf.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class JpaTeeServiceImpl implements TeeService {

  private final TeeRepository teeRepository;
  private final CourseRepository courseRepository;
  private final TeeMapper teeMapper;

  JpaTeeServiceImpl(
      TeeRepository teeRepository, CourseRepository courseRepository, TeeMapper teeMapper) {
    this.teeRepository = teeRepository;
    this.courseRepository = courseRepository;
    this.teeMapper = teeMapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<TeeDto> listByCourse(UUID courseId) {
    return teeRepository.findByCourse_CourseId(courseId).stream().map(teeMapper::map).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TeeDto> get(UUID teeId) {
    return teeMapper.map(teeRepository.findById(teeId));
  }

  @Override
  @Transactional
  public Optional<TeeDto> create(UUID courseId, CreateTeeRequest request) {
    return courseRepository
        .findById(courseId)
        .map(course -> teeMapper.map(teeRepository.save(teeMapper.map(course, request))));
  }

  @Override
  @Transactional
  public Optional<TeeDto> patch(UUID teeId, PatchTeeRequest request) {
    Optional<TeeEntity> toUpdate =
        teeRepository
            .findById(teeId)
            .map(
                t -> {
                  t.setName(request.name().orElse(t.getName()));
                  t.setPar(request.par().orElse(t.getPar()));
                  t.setYardage(request.yardage().orElse(t.getYardage()));
                  t.setSlope(request.slope().orElse(t.getSlope()));
                  t.setRating(request.rating().orElse(t.getRating()));
                  return t;
                });
    return toUpdate.map(t -> teeMapper.map(teeRepository.save(t)));
  }

  @Override
  @Transactional
  public void delete(UUID teeId) {
    teeRepository.deleteById(teeId);
  }
}
