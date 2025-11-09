package com.alimmit.golf.scorecard;

import static com.alimmit.golf.scorecard.ScorecardConstants.SCORECARD_ENDPOINT;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.errors.NotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(SCORECARD_ENDPOINT)
@Tag(name = "Scorecards")
class ScorecardController {

  private static final Map<String, List<ScorecardDto>> scores = new HashMap<>();

  private final ScorecardIdGenerator scorecardIdGenerator;

  public ScorecardController(ScorecardIdGenerator scorecardIdGenerator) {
    this.scorecardIdGenerator = scorecardIdGenerator;
  }

  @Operation(method = "GET", operationId = "scorecard.create", summary = "Create new scorecard", description = "Add a new score for a round of golf", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ScorecardRequestDto.class, contentMediaType = "application/json"))))
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

  @Operation(method = "GET", operationId = "scorecard.list", summary = "List your scorecards", description = "Get a list of your scorecards")
  @GetMapping
  List<ScorecardDto> list(Authentication authentication) {
    return scores.computeIfAbsent(authentication.getName(), key -> new ArrayList<>());
  }

  @Operation(method = "GET", operationId = "scorecard.get", summary = "Get scorecord", description = "Get one of your scorecards")
  @GetMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  ScorecardDto get(@PathVariable String id) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    List<ScorecardDto> userScores = scores.computeIfAbsent(auth.getName(), key -> new ArrayList<>())
        .stream()
        .filter(score -> id.equals(score.scorecardId()))
        .toList();

    if (userScores.isEmpty()) {
      throw new NotFoundException();
    } else if (userScores.size() == 1) {
      return userScores.getFirst();
    } else {
      throw new IllegalStateException("This won't happen once there's a DB");
    }
  }

  @Operation(method = "DELETE", operationId = "scorecard.delete", summary = "Delete a scorecard", description = "Delete one of your scorecards")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  void delete(@PathVariable String id) {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Do get first to ensure id is valid
    this.get(id);

    scores.computeIfAbsent(auth.getName(), key -> new ArrayList<>())
        .removeIf(predicate -> id.equals(predicate.scorecardId()));
  }
}
