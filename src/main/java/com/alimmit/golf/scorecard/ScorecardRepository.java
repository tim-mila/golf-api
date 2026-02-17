package com.alimmit.golf.scorecard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Scorecard entities with automatic user-scoping via Spring Security. Uses SpEL
 * expressions (?#{principal.name}) to automatically filter by current authenticated user.
 */
@Repository
interface ScorecardRepository extends JpaRepository<ScorecardEntity, String> {

  /**
   * Find all scorecards for the currently authenticated user. Uses SpEL to automatically inject the
   * user ID from SecurityContext.
   *
   * @return list of scorecards for the current user
   */
  @Query("SELECT s FROM ScorecardEntity s WHERE s.createdBy = ?#{authentication.name}")
  List<ScorecardEntity> findAllForCurrentUser();

  /**
   * Find all scorecards for the provided user
   *
   * @param createdBy User that created the scorecards
   * @return list of scorecards for the provided user
   */
  @Query("SELECT s FROM ScorecardEntity s WHERE s.createdBy = :createdBy")
  List<ScorecardEntity> findAllCreatedBy(@Param("createdBy") String createdBy);

  /**
   * Find a scorecard by ID for the currently authenticated user. Uses SpEL to automatically inject
   * the user ID from SecurityContext. This ensures users can only access their own scorecards.
   *
   * @param scorecardId the scorecard ID
   * @return the scorecard if found and belongs to current user
   */
  @Query(
      "SELECT s FROM ScorecardEntity s WHERE s.scorecardId = :scorecardId AND s.createdBy = ?#{authentication.name}")
  Optional<ScorecardEntity> findByIdForCurrentUser(@Param("scorecardId") UUID scorecardId);

  /**
   * Delete a scorecard by ID for the currently authenticated user. Uses SpEL to automatically
   * inject the user ID from SecurityContext. This ensures users can only delete their own
   * scorecards.
   *
   * @param scorecardId the scorecard ID
   * @return number of records deleted (0 if not found or not owned by user)
   */
  @Modifying
  @Query(
      "DELETE FROM ScorecardEntity s WHERE s.scorecardId = :scorecardId AND s.createdBy = ?#{authentication.name}")
  int deleteByIdForCurrentUser(@Param("scorecardId") UUID scorecardId);
}
