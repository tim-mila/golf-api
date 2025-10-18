package com.alimmit.golf.scorecard;

import static com.alimmit.golf.scorecard.ScorecardConstants.SCORECARD_ENDPOINT;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping(SCORECARD_ENDPOINT)
class ScorecardController {

  private static final Map<String, List<ScorecardDto>> scores = new HashMap<>();

  private final ScorecardIdGenerator scorecardIdGenerator;

  public ScorecardController(ScorecardIdGenerator scorecardIdGenerator) {
    this.scorecardIdGenerator = scorecardIdGenerator;
  }

  @PostMapping
  ScorecardDto create(@RequestBody @Valid ScorecardRequestDto request) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    ScorecardDto scorecardDto = new ScorecardDto(
        scorecardIdGenerator.generate(),
        Instant.now(),
        auth.getName(),
        Instant.now(),
        auth.getName(),
        request.scoreDate(),
        request.courseId(),
        request.score());

    List<ScorecardDto> userScores = scores.computeIfAbsent(auth.getName(), key -> new ArrayList<>());
    userScores.add(scorecardDto);
    scores.put(auth.getName(), userScores);

    return scorecardDto;
  }

  @GetMapping
  List<ScorecardDto> list(Authentication authentication) {
    return scores.computeIfAbsent(authentication.getName(), key -> new ArrayList<>());
  }

}
