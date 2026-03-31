package com.alimmit.golf.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TeeRepository extends JpaRepository<TeeEntity, UUID> {

  void deleteByCourse_Id(UUID courseId);

  @Query(
      "SELECT t FROM TeeEntity t WHERE t.course.id = :courseId AND t.course.createdBy = ?#{authentication.name}")
  List<TeeEntity> findByCourseIdForCurrentUser(@Param("courseId") UUID courseId);

  @Query(
      "SELECT t FROM TeeEntity t WHERE t.id = :teeId AND t.course.createdBy = ?#{authentication.name}")
  Optional<TeeEntity> findByIdForCurrentUser(@Param("teeId") UUID teeId);

  @Modifying
  @Query(
      "DELETE FROM TeeEntity t WHERE t.id = :teeId AND t.course.createdBy = ?#{authentication.name}")
  int deleteByIdForCurrentUser(@Param("teeId") UUID teeId);
}
