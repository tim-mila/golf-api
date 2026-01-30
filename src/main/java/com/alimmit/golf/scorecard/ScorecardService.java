package com.alimmit.golf.scorecard;

import java.util.List;
import java.util.Optional;

public interface ScorecardService {

    /**
     * Create a new scorecard.
     * Generates ID and persists to database with audit fields.
     * If the rating and slope are not provided, they are looked up from the golf course API.
     */
    ScorecardDto create(ScorecardRequestDto request);

    /**
     * List all scorecards for the current authenticated user.
     *
     * @return List of scorecards
     */
    List<ScorecardDto> listAll();

    /**
     * List all scorecards for the provided user
     *
     * @param userId User identifier
     * @return List of scorecards
     */
    List<ScorecardDto> listAll(String userId);

    /**
     * Get a scorecard by identifier
     *
     * @param id Scorecard identifier
     * @return Optional of scorecard
     */
    Optional<ScorecardDto> getById(String id);

    /**
     * Delete a scorecard
     *
     * @param id Scorecard identifier
     */
    int deleteById(String id);
}
