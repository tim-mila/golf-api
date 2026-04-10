package com.alimmit.golf.scorecard;

import java.util.Optional;
import java.util.UUID;

public interface ScorecardService {

  /**
   * Create a new scorecard. Generates ID and persists to database with audit fields. If the rating
   * and slope are not provided, they are looked up from the golf course API.
   */
  ScorecardDto create(ScorecardRequestDto request);

  /**
   * List scorecards for the current authenticated user using keyset pagination, ordered by {@code
   * scoreDate DESC, id DESC}.
   *
   * @param limit maximum number of results (1–100)
   * @param cursor keyset cursor from the previous page; null for the first page
   * @return paginated result with an opaque next-cursor when more results exist
   */
  ScorecardPageDto list(int limit, ScorecardCursor cursor);

  /**
   * Get a scorecard by identifier
   *
   * @param id Scorecard identifier
   * @return Optional of scorecard
   */
  Optional<ScorecardDto> getById(UUID id);

  /**
   * Delete a scorecard
   *
   * @param id Scorecard identifier
   */
  int deleteById(UUID id);
}
