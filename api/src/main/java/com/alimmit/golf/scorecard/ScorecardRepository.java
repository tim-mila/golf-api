package com.alimmit.golf.scorecard;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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
interface ScorecardRepository extends JpaRepository<ScorecardEntity, UUID> {

  /**
   * Find all scorecards for the currently authenticated user. Uses SpEL to automatically inject the
   * user ID from SecurityContext.
   *
   * @return list of scorecards for the current user
   */
  @Query("SELECT s FROM ScorecardEntity s WHERE s.createdBy = ?#{authentication.name}")
  List<ScorecardEntity> findAllForCurrentUser();

  /**
   * Find the most recent scorecards for the provided user, ordered by score date descending. Use
   * {@link org.springframework.data.domain.PageRequest#of(int, int)} to bound the result set.
   *
   * @param createdBy User that created the scorecards
   * @param pageable pagination/limit descriptor
   * @return bounded list of most recent scorecards for the provided user
   */
  @Query("SELECT s FROM ScorecardEntity s WHERE s.createdBy = :createdBy ORDER BY s.scoreDate DESC")
  List<ScorecardEntity> findMostRecentForUser(
      @Param("createdBy") String createdBy, Pageable pageable);

  /**
   * First page of scorecards for the current user, ordered by {@code scoreDate DESC, id DESC}.
   * Fetch {@code limit + 1} rows to determine whether a next page exists.
   */
  @Query(
      """
      SELECT s FROM ScorecardEntity s
      WHERE s.createdBy = ?#{authentication.name}
      ORDER BY s.scoreDate DESC, s.id DESC
      """)
  List<ScorecardEntity> findFirstPageForCurrentUser(Pageable pageable);

  /**
   * Subsequent page of scorecards for the current user using keyset pagination. Returns records
   * strictly before the cursor position {@code (cursorDate, cursorId)} in the sort order {@code
   * scoreDate DESC, id DESC}.
   */
  @Query(
      """
      SELECT s FROM ScorecardEntity s
      WHERE s.createdBy = ?#{authentication.name}
        AND (s.scoreDate < :cursorDate
          OR (s.scoreDate = :cursorDate AND s.id < :cursorId))
      ORDER BY s.scoreDate DESC, s.id DESC
      """)
  List<ScorecardEntity> findNextPageForCurrentUser(
      @Param("cursorDate") LocalDate cursorDate,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);

  /**
   * Find a scorecard by ID for the currently authenticated user. Uses SpEL to automatically inject
   * the user ID from SecurityContext. This ensures users can only access their own scorecards.
   *
   * @param scorecardId the scorecard ID
   * @return the scorecard if found and belongs to current user
   */
  @Query(
      "SELECT s FROM ScorecardEntity s WHERE s.id = :scorecardId AND s.createdBy = ?#{authentication.name}")
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
      "DELETE FROM ScorecardEntity s WHERE s.id = :scorecardId AND s.createdBy = ?#{authentication.name}")
  int deleteByIdForCurrentUser(@Param("scorecardId") UUID scorecardId);
}
