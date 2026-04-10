package com.alimmit.golf.scorecard;

import static com.alimmit.golf.scorecard.ScorecardConstants.SCORECARD_ENDPOINT;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.security.CanRead;
import com.alimmit.golf.security.CanWrite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(SCORECARD_ENDPOINT)
@Tag(name = "Scorecards")
class ScorecardController {

  private static final Logger logger = LoggerFactory.getLogger(ScorecardController.class);

  private final ScorecardService scorecardService;
  private final ScorecardEventPublisher scorecardEventPublisher;

  ScorecardController(
      ScorecardService scorecardService, ScorecardEventPublisher scorecardEventPublisher) {
    this.scorecardService = scorecardService;
    this.scorecardEventPublisher = scorecardEventPublisher;
  }

  @Operation(
      method = "POST",
      operationId = "scorecard.create",
      summary = "Create new scorecard",
      description = "Add a new score for a round of golf",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ScorecardRequestDto.class))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ScorecardDto.class))),
        @ApiResponse(responseCode = "400", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content)
      })
  @CanWrite(GlobalConstants.SCOPE_SCORECARD)
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  ScorecardDto create(@RequestBody @Valid ScorecardRequestDto request) {
    logger.debug("create scorecard");
    ScorecardDto created = scorecardService.create(request);
    scorecardEventPublisher.publishCreated(created);
    return created;
  }

  @Operation(
      method = "GET",
      operationId = "scorecard.list",
      summary = "List your scorecards",
      description =
          "Get a paginated list of your scorecards ordered by score date descending. "
              + "Pass the `nextCursor` from the previous response as `cursor` to retrieve the next page.",
      parameters = {
        @Parameter(
            name = "limit",
            description = "Maximum number of results to return (1–100)",
            in = ParameterIn.QUERY,
            schema =
                @Schema(type = "integer", defaultValue = "20", minimum = "1", maximum = "100")),
        @Parameter(
            name = "cursor",
            description =
                "Opaque pagination cursor returned as `nextCursor` in the previous response",
            in = ParameterIn.QUERY)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ScorecardPageDto.class))),
        @ApiResponse(responseCode = "400", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_SCORECARD)
  @GetMapping
  ScorecardPageDto list(
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
      @RequestParam(required = false) String cursor) {
    ScorecardCursor decoded = cursor != null ? ScorecardCursor.decode(cursor) : null;
    return scorecardService.list(limit, decoded);
  }

  @Operation(
      method = "GET",
      operationId = "scorecard.get",
      summary = "Get scorecard",
      description = "Get one of your scorecards",
      parameters = {
        @Parameter(name = "id", description = "Scorecard identifier", in = ParameterIn.PATH)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ScorecardDto.class))),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_SCORECARD)
  @GetMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  ScorecardDto get(@PathVariable UUID id) {
    return scorecardService.getById(id).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "DELETE",
      operationId = "scorecard.delete",
      summary = "Delete a scorecard",
      description = "Delete one of your scorecards",
      parameters = {
        @Parameter(name = "id", description = "Scorecard identifier", in = ParameterIn.PATH)
      },
      responses = {
        @ApiResponse(responseCode = "204", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanWrite(GlobalConstants.SCOPE_SCORECARD)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  void delete(@PathVariable UUID id) {
    logger.debug("delete scorecard | id={}", id);
    int deleted = scorecardService.deleteById(id);
    if (deleted == 1) {
      scorecardEventPublisher.publishedDeleted();
    } else if (deleted == 0) {
      throw new NotFoundException();
    } else if (deleted > 1) {
      throw new IllegalStateException();
    }
  }
}
