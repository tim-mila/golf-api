package com.alimmit.golf.handicap;

import com.alimmit.golf.errors.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
      summary = "Get handicap handicapIndex",
      description = "Get the most recent calculated handicap handicapIndex for the currently logged in user")
  @GetMapping
  HandicapDto getHandicap() {
    return handicapService.getHandicap().orElseThrow(NotFoundException::new);
  }
}
