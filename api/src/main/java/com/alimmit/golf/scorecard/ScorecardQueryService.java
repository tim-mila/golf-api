package com.alimmit.golf.scorecard;

import java.util.List;

/**
 * Internal service for fetching scorecards by a specific user ID. Intended for use by async event
 * processors (e.g. handicap recalculation) that operate outside the current HTTP request context.
 * Not part of the public {@link ScorecardService} interface — not reachable via any HTTP endpoint.
 */
public interface ScorecardQueryService {

  /**
   * Find all scorecards for the given user.
   *
   * @param userId the user identifier
   * @return list of scorecards belonging to the user
   */
  List<ScorecardDto> findAllForUser(String userId);
}
