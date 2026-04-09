package com.alimmit.golf.course;

import com.alimmit.golf.GlobalConstants;
import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.security.CanRead;
import com.alimmit.golf.security.CanWrite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Tees")
class TeeController {

  private static final Logger logger = LoggerFactory.getLogger(TeeController.class);

  private final TeeService teeService;

  TeeController(TeeService teeService) {
    this.teeService = teeService;
  }

  @Operation(
      method = "GET",
      operationId = "tee.listByCourse",
      summary = "List tees for a course",
      description = "Get all tees for a given golf course",
      parameters = {
        @Parameter(
            name = "courseId",
            description = "Golf course identifier",
            in = ParameterIn.QUERY,
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = TeeDto.class)))),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_COURSE)
  @GetMapping(path = TeeConstants.TEE_ENDPOINT)
  List<TeeDto> listByCourse(@RequestParam UUID courseId) {
    return teeService.listByCourse(courseId);
  }

  @Operation(
      method = "GET",
      operationId = "tee.get",
      summary = "Get a tee",
      description = "Get a tee by identifier",
      parameters = {@Parameter(name = "id", description = "Tee identifier", in = ParameterIn.PATH)},
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TeeDto.class))),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanRead(GlobalConstants.SCOPE_COURSE)
  @GetMapping(path = TeeConstants.TEE_BY_ID_ENDPOINT)
  TeeDto get(@PathVariable("id") UUID teeId) {
    return teeService.get(teeId).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "POST",
      operationId = "tee.create",
      summary = "Add a new tee to a course",
      description = "Add a new tee to a golf course",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = CreateTeeRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "201",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TeeDto.class))),
        @ApiResponse(responseCode = "400", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @ResponseStatus(code = HttpStatus.CREATED)
  @PostMapping(path = TeeConstants.TEE_ENDPOINT)
  TeeDto create(@Valid @RequestBody CreateTeeRequest request) {
    logger.debug("create tee | courseId={}", request.courseId());
    return teeService.create(request).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "PATCH",
      operationId = "tee.patch",
      summary = "Partially update a tee",
      description = "Update the tee with the provided fields",
      parameters = {@Parameter(name = "id", description = "Tee identifier", in = ParameterIn.PATH)},
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = PatchTeeRequest.class))),
      responses = {
        @ApiResponse(
            responseCode = "200",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TeeDto.class))),
        @ApiResponse(responseCode = "400", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @PatchMapping(path = TeeConstants.TEE_BY_ID_ENDPOINT)
  TeeDto patch(@Valid @RequestBody PatchTeeRequest request, @PathVariable("id") UUID teeId) {
    logger.debug("patch tee | id={}", teeId);
    return teeService.patch(teeId, request).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "DELETE",
      operationId = "tee.delete",
      summary = "Delete a tee",
      description = "Delete a tee by identifier",
      parameters = {@Parameter(name = "id", description = "Tee identifier", in = ParameterIn.PATH)},
      responses = {
        @ApiResponse(responseCode = "204", content = @Content),
        @ApiResponse(responseCode = "401", content = @Content),
        @ApiResponse(responseCode = "403", content = @Content),
        @ApiResponse(responseCode = "404", content = @Content)
      })
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @DeleteMapping(path = TeeConstants.TEE_BY_ID_ENDPOINT)
  void delete(@PathVariable("id") UUID teeId) {
    logger.debug("delete tee | id={}", teeId);
    teeService.delete(teeId);
  }
}
