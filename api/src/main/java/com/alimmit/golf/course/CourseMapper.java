package com.alimmit.golf.course;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class CourseMapper {

  CourseEntity map(CreateCourseRequest request) {
    return new CourseEntity(request.club(), request.course(), request.city(), request.state());
  }

  Optional<CourseDto> map(Optional<CourseEntity> entity) {
    return entity.map(this::map);
  }

  CourseDto map(CourseEntity entity) {
    return new CourseDto(
        entity.getId(),
        entity.getCreatedAt(),
        entity.getLastModifiedAt(),
        entity.getClub(),
        entity.getCourse(),
        entity.getCity(),
        entity.getState());
  }
}
