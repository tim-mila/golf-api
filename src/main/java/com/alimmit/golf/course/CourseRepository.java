package com.alimmit.golf.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

  @Query("SELECT c FROM CourseEntity c WHERE c.createdBy = ?#{authentication.name}")
  List<CourseEntity> findAllForCurrentUser();

  @Query(
      "SELECT c FROM CourseEntity c WHERE c.id = :courseId AND c.createdBy = ?#{authentication.name}")
  Optional<CourseEntity> findByIdForCurrentUser(@Param("courseId") UUID courseId);
}
