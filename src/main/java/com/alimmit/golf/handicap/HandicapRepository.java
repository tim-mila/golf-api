package com.alimmit.golf.handicap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface HandicapRepository extends JpaRepository<HandicapEntity, String> {

  /**
   * Find the most recent calculated handicap for the currently authenticated user.
   * Uses SpEL to automatically inject the user ID from SecurityContext.
   *
   * @return optional, the most recent calculated handicap
   */
  @Query("SELECT h FROM HandicapEntity h WHERE h.golferId = ?#{authentication.name} ORDER BY h.createdAt DESC LIMIT 1")
  Optional<HandicapEntity> findHandicapForCurrentUser();

  /**
   * Find all calculated handicaps for the currently authenticated user.
   * Uses SpEL to automatically inject the user ID from SecurityContext.
   *
   * @return list of handicaps for the current user
   */
  @Query("SELECT h FROM HandicapEntity h WHERE h.golferId = ?#{authentication.name}")
  List<HandicapEntity> findAllForCurrentUser();

  /**
   * Find a handicap by ID for the currently authenticated user.
   * Uses SpEL to automatically inject the user ID from SecurityContext.
   * This ensures users can only access their own handicaps.
   *
   * @param handicapId the handicap ID
   * @return the handicap if found and belongs to current user
   */
  @Query("SELECT h FROM HandicapEntity h WHERE h.handicapId = :handicapId AND h.golferId = ?#{authentication.name}")
  Optional<HandicapEntity> findByIdForCurrentUser(@Param("handicapId") String handicapId);
}
