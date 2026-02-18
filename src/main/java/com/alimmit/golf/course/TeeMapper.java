package com.alimmit.golf.course;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class TeeMapper {

  TeeEntity map(CourseEntity course, CreateTeeRequest request) {
    return new TeeEntity(
        course, request.name(), request.par(), request.yardage(), request.slope(), request.rating());
  }

  Optional<TeeDto> map(Optional<TeeEntity> entity) {
    return entity.map(this::map);
  }

  TeeDto map(TeeEntity entity) {
    return new TeeDto(
        entity.getTeeId(),
        entity.getCourse().getCourseId(),
        entity.getCreatedAt(),
        entity.getLastModifiedAt(),
        entity.getName(),
        entity.getPar(),
        entity.getYardage(),
        entity.getSlope(),
        entity.getRating());
  }
}
