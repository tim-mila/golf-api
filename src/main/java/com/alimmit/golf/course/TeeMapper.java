package com.alimmit.golf.course;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class TeeMapper {

  TeeEntity map(CourseEntity course, CreateTeeRequest request) {
    return new TeeEntity(course, request.name(), request.par(), request.slope(), request.rating());
  }

  Optional<TeeDto> map(Optional<TeeEntity> entity) {
    return entity.map(this::map);
  }

  TeeDto map(TeeEntity entity) {
    return new TeeDto(
        entity.getId(),
        entity.getCourse().getId(),
        entity.getCreatedAt(),
        entity.getLastModifiedAt(),
        entity.getName(),
        entity.getPar(),
        entity.getSlope(),
        entity.getRating());
  }
}
