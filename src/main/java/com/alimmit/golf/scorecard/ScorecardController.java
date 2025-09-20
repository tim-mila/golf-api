package com.alimmit.golf.scorecard;

import static com.alimmit.golf.scorecard.ScorecardConstants.SCORECARD_ENDPOINT;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RequestMapping(SCORECARD_ENDPOINT)
class ScorecardController {

  private static final Map<String, List<ScorecardDto>> scores = new HashMap<>();

  @PostMapping
  ScorecardDto create(@Valid @RequestBody ScorecardRequestDto request, Principal principal) {

    ScorecardDto scorecardDto =
        new ScorecardDto(UUID.randomUUID(), Instant.now(), principal.getName(), Instant.now(),
            principal.getName(), request.scoreDate(), request.courseId(), request.score());

    List<ScorecardDto> userScores =
        scores.computeIfAbsent(principal.getName(), key -> new ArrayList<>());
    userScores.add(scorecardDto);
    scores.put(principal.getName(), userScores);

    return scorecardDto;
  }

}
