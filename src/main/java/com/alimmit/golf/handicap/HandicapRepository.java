package com.alimmit.golf.handicap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface HandicapRepository extends JpaRepository<HandicapEntity, String> {

  /**
   * Find calculated handicap for the currently authenticated user.
   * Uses SpEL to automatically inject the user ID from SecurityContext.
   *
   * @return optional, calculated handicap
   */
  @Query("SELECT h FROM HandicapEntity h WHERE h.golferId = ?#{authentication.name} ORDER BY h.createdAt DESC LIMIT 1")
  Optional<HandicapEntity> findHandicapForCurrentUser();

  /**
   * Find calculated handicap for the provided user.
   *
   * @param golferId golfer identifier
   * @return optional, calculated handicap
   */
  @Query("SELECT h FROM HandicapEntity h WHERE h.golferId = :golferId")
  Optional<HandicapEntity> findHandicapForUser(@Param("golferId") String golferId);
}
