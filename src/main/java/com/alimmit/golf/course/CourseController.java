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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = CourseConstants.COURSE_ENDPOINT)
@Tag(name = "Courses")
class CourseController {

  private final CourseService courseService;

  CourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @Operation(
      method = "GET",
      operationId = "course.list",
      summary = "List golf courses",
      description = "Get the list of all golf courses",
      responses =
          @ApiResponse(
              responseCode = "200",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      array = @ArraySchema(schema = @Schema(implementation = CourseDto.class)))))
  @CanRead(GlobalConstants.SCOPE_COURSE)
  @GetMapping
  List<CourseDto> list() {
    return courseService.list();
  }

  @Operation(
      method = "GET",
      operationId = "course.get",
      summary = "Get a golf course",
      description = "Get a golf courses by identifier",
      parameters = {
        @Parameter(name = "id", description = "Golf course identifier", in = ParameterIn.PATH)
      },
      responses =
          @ApiResponse(
              responseCode = "200",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = CourseDto.class))))
  @CanRead(GlobalConstants.SCOPE_COURSE)
  @GetMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  CourseDto get(@PathVariable("id") UUID courseId) {
    return courseService.get(courseId).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "POST",
      operationId = "course.create",
      summary = "Add a new golf course",
      description = "Add a new golf course",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = CreateCourseRequest.class))),
      responses =
          @ApiResponse(
              responseCode = "201",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = CourseDto.class))))
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @ResponseStatus(code = HttpStatus.CREATED)
  @PostMapping
  CourseDto create(@Valid @RequestBody CreateCourseRequest request) {
    return courseService.create(request);
  }

  @Operation(
      method = "PATCH",
      operationId = "course.patch",
      summary = "Partially update a golf course",
      description = "Update the golf course with the provided fields",
      parameters = {
        @Parameter(name = "id", description = "Golf course identifier", in = ParameterIn.PATH)
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = PatchCourseRequest.class))),
      responses =
          @ApiResponse(
              responseCode = "200",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = CourseDto.class))))
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @PatchMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  CourseDto patch(
      @Valid @RequestBody PatchCourseRequest request, @PathVariable("id") UUID courseId) {
    return courseService.patch(courseId, request).orElseThrow(NotFoundException::new);
  }

  @Operation(
      method = "DELETE",
      operationId = "course.delete",
      summary = "Delete a golf course",
      description = "Delete a golf course and all associated tees",
      parameters = {
        @Parameter(name = "id", description = "Golf course identifier", in = ParameterIn.PATH)
      },
      responses = @ApiResponse(responseCode = "204"))
  @CanWrite(GlobalConstants.SCOPE_COURSE)
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @DeleteMapping(path = GlobalConstants.API_RECORD_SUFFIX)
  void delete(@PathVariable("id") UUID courseId) {
    courseService.delete(courseId);
  }
}
