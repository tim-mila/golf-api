package com.alimmit.golf.scorecard;

import java.util.List;

/**
 * Internal service for fetching scorecards by a specific user ID. Intended for use by async event
 * processors (e.g. handicap recalculation) that operate outside the current HTTP request context.
 * Not part of the public {@link ScorecardService} interface — not reachable via any HTTP endpoint.
 */
public interface ScorecardQueryService {

  /**
   * Find the most recent scorecards for the given user, ordered by score date descending.
   *
   * @param userId the user identifier
   * @param limit maximum number of scorecards to return
   * @return bounded list of most recent scorecards belonging to the user
   */
  List<ScorecardDto> findMostRecentForUser(String userId, int limit);
}
