package com.alimmit.golf.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface TeeService {

  /**
   * List all tees for a given course
   *
   * @param courseId Golf course identifier
   * @return list of TeeDto
   */
  List<TeeDto> listByCourse(UUID courseId);

  /**
   * Get a single tee by identifier
   *
   * @param teeId Tee identifier
   * @return optional, TeeDto
   */
  Optional<TeeDto> get(UUID teeId);

  /**
   * Create a new tee for a course
   *
   * @param request Create tee request (includes courseId)
   * @return TeeDto
   */
  Optional<TeeDto> create(CreateTeeRequest request);

  /**
   * Partially update a tee
   *
   * @param teeId Tee identifier
   * @param request Patch tee request
   * @return Patched TeeDto
   */
  Optional<TeeDto> patch(UUID teeId, PatchTeeRequest request);

  /**
   * Delete a tee
   *
   * @param teeId Tee identifier
   */
  void delete(UUID teeId);
}
