package com.alimmit.golf.scorecard;

import static com.alimmit.golf.scorecard.ScorecardConstants.SCORECARD_ENDPOINT;

import java.util.List;

import com.alimmit.golf.errors.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.alimmit.golf.GlobalConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping(SCORECARD_ENDPOINT)
@Tag(name = "Scorecards")
class ScorecardController {

  private final ScorecardService scorecardService;
  private final ScorecardEventPublisher scorecardEventPublisher;

  ScorecardController(ScorecardService scorecardService, ScorecardEventPublisher scorecardEventPublisher) {
    this.scorecardService = scorecardService;
    this.scorecardEventPublisher = scorecardEventPublisher;
  }

  @Operation(method = "POST", operationId = "scorecard.create", summary = "Create new scorecard", description = "Add a new score for a round of golf", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ScorecardRequestDto.class, contentMediaType = "application/json"))))
  @PostMapping
  ScorecardDto create(@RequestBody @Valid ScorecardRequestDto request) {
    ScorecardDto created = scorecardService.create(request);
    scorecardEventPublisher.publishCreated(created);
    return created;
  }

  @Operation(method = "GET", operationId = "scorecard.list", summary = "List your scorecards", description = "Get a list of your scorecards")
  @GetMapping
  List<ScorecardDto> list() {
    return scorecardService.listAll();
  }

  @Operation(method = "GET", operationId = "scorecard.get", summary = "Get scorecard", description = "Get one of your scorecards")
  @GetMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  ScorecardDto get(@PathVariable String id) {
    return scorecardService.getById(id).orElseThrow(NotFoundException::new);
  }

  @Operation(method = "DELETE", operationId = "scorecard.delete", summary = "Delete a scorecard", description = "Delete one of your scorecards")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  void delete(@PathVariable String id) {
    int deleted = scorecardService.deleteById(id);
    if (deleted == 0) {
      throw new NotFoundException();
    } else if (deleted > 1) {
      throw new IllegalStateException();
    }
  }
}
