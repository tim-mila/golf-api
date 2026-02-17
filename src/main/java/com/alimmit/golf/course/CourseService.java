package com.alimmit.golf.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseService {

  /**
   * List all persistent golf courses
   *
   * @return list of CourseDto
   */
  List<CourseDto> list();

  /**
   * Get a single golf course by identifier
   *
   * @param courseId Golf course identifier
   * @return optional, CourseDto
   */
  Optional<CourseDto> get(UUID courseId);

  /**
   * Create new golf course
   *
   * @param request Create golf course request
   * @return CourseDto
   */
  CourseDto create(CreateCourseRequest request);

  /**
   * Partially update a golf course
   *
   * @param courseId Golf course identifier
   * @param request Patch golf course request
   * @return Patched CourseDto
   */
  Optional<CourseDto> patch(UUID courseId, PatchCourseRequest request);

  /**
   * Delete a golf course
   *
   * @param courseId Golf course identifier
   */
  void delete(UUID courseId);
}
