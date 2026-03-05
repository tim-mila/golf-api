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

  @Query(
      "SELECT COUNT(c) > 0 FROM CourseEntity c WHERE c.createdBy = ?#{authentication.name}"
          + " AND c.club = :club AND c.course = :course AND c.city = :city AND c.state = :state")
  boolean existsByUniqueConstraintForCurrentUser(
      @Param("club") String club,
      @Param("course") String course,
      @Param("city") String city,
      @Param("state") USState state);

  @Query(
      value =
          """
          SELECT course_id, created_at, created_by, last_modified_at, last_modified_by,
                 club, course, city, state
          FROM course
          WHERE created_by = ?#{authentication.name}
          AND search_vector @@ to_tsquery('english', :query)
          ORDER BY ts_rank(search_vector, to_tsquery('english', :query)) DESC
          """,
      nativeQuery = true)
  List<CourseEntity> search(@Param("query") String query);
}
