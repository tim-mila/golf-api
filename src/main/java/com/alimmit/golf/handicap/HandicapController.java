package com.alimmit.golf.handicap;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.security.CanRead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(HandicapConstants.HANDICAP_ENDPOINT)
@Tag(name = "Handicap")
class HandicapController {

  private final HandicapService handicapService;

  HandicapController(HandicapService handicapService) {
    this.handicapService = handicapService;
  }

  @Operation(
      method = "GET",
      operationId = "handicap.get",
      summary = "Get handicap index",
      description = "Get the most recent handicap index for the currently logged in user",
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = HandicapDto.class))),
        @ApiResponse(
            responseCode = "204",
            content = @Content,
            description = "Golfer does not have established handicap index"),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_HANDICAP)
  @GetMapping
  ResponseEntity<HandicapDto> getHandicap() {
    Optional<HandicapDto> handicap = handicapService.getHandicap();
    return handicap.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
  }

  @Operation(
      method = "GET",
      operationId = "handicap.get.history",
      summary = "Get handicap index history",
      description = "Get the history of handicap indexes for the currently logged in user",
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(
                            schema = @Schema(implementation = HandicapRevisionDto.class)))),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_HANDICAP)
  @GetMapping(path = GlobalConstants.API_HISTORY_SUFFIX)
  List<HandicapRevisionDto> history() {
    return handicapService.getHistory();
  }
}
