package com.alimmit.golf.course;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.utils.JwtPersona;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(CourseController.class)
@Import(MethodSecurityConfiguration.class)
class CourseControllerTest extends AbstractCourseControllerTest {

  @MockitoBean private CourseService courseService;

  @Autowired
  public CourseControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void listCourses() throws Exception {
    when(courseService.list())
        .thenReturn(
            List.of(
                new CourseDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    Instant.now(),
                    "Club 1",
                    "Course 1",
                    "Anytown",
                    USState.WISCONSIN)));

    listCourses(JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpectAll(
            jsonPath("$.[0].club").value("Club 1"),
            jsonPath("$.[0].course").value("Course 1"),
            jsonPath("$.[0].city").value("Anytown"),
            jsonPath("$.[0].state").value(USState.WISCONSIN.getAbbreviation()));
  }

  @Test
  void getCourse_ExpectOk() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(courseService.get(courseId))
        .thenReturn(
            Optional.of(
                new CourseDto(
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "Club 1",
                    "Course 1",
                    "Anytown",
                    USState.WISCONSIN)));

    getCourse(courseId, JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.club").value("Club 1"),
            jsonPath("$.course").value("Course 1"),
            jsonPath("$.city").value("Anytown"),
            jsonPath("$.state").value(USState.WISCONSIN.getAbbreviation()));
  }

  @Test
  void getCourse_ExpectNotFound() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(courseService.get(courseId)).thenReturn(Optional.empty());
    getCourse(courseId, JwtPersona.forAmyAdmin()).andExpect(status().isNotFound());
  }

  @Test
  void createCourse_ExpectedCreated() throws Exception {
    when(courseService.create(
            argThat(
                m ->
                    m.club().equals("Acme Club")
                        && m.course().equals("Acme Course")
                        && m.city().equals("Someplace")
                        && m.state() == USState.MICHIGAN)))
        .thenReturn(
            new CourseDto(
                UUID.randomUUID(),
                Instant.now(),
                Instant.now(),
                "Acme Club",
                "Acme Course",
                "Someplace",
                USState.MICHIGAN));

    String requestBody =
        """
        {
          "club": "Acme Club",
          "course": "Acme Course",
          "city": "Someplace",
          "state": "MI"
        }
        """;

    createCourse(requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isCreated())
        .andExpectAll(
            jsonPath("$.club").value("Acme Club"),
            jsonPath("$.course").value("Acme Course"),
            jsonPath("$.city").value("Someplace"),
            jsonPath("$.state").value(USState.MICHIGAN.getAbbreviation()),
            jsonPath("$.courseId").exists(),
            jsonPath("$.createdAt").exists(),
            jsonPath("$.lastModifiedAt").exists());
  }

  @Test
  void patchCourse_ExpectOk() throws Exception {

    UUID courseId = UUID.randomUUID();
    when(courseService.patch(
            eq(courseId),
            argThat(
                m ->
                    m.club().orElseThrow().equals("Acme Club")
                        && m.course().orElseThrow().equals("Acme Course")
                        && m.city().orElseThrow().equals("Someplace")
                        && m.state().orElseThrow() == USState.MICHIGAN)))
        .thenReturn(
            Optional.of(
                new CourseDto(
                    UUID.randomUUID(),
                    Instant.now(),
                    Instant.now(),
                    "Acme Club",
                    "Acme Course",
                    "Someplace",
                    USState.MICHIGAN)));

    String requestBody =
        """
        {
          "club": "Acme Club",
          "course": "Acme Course",
          "city": "Someplace",
          "state": "MI"
        }
        """;
    patchCourse(courseId, requestBody, JwtPersona.forAmyAdmin()).andExpect(status().isOk());
  }

  @Test
  void deleteCourse_ExpectNoContent() throws Exception {
    UUID courseId = UUID.randomUUID();
    doNothing().when(courseService).delete(courseId);
    deleteCourse(courseId, JwtPersona.forAmyAdmin()).andExpect(status().isNoContent());
  }

  // --- Unauthenticated tests ---

  @ParameterizedTest
  @MethodSource
  void unauthenticated_ExpectUnauthorized(MockHttpServletRequestBuilder request) throws Exception {
    performWithoutAuth(request).andExpect(status().isUnauthorized());
  }

  static Stream<MockHttpServletRequestBuilder> unauthenticated_ExpectUnauthorized() {
    UUID id = UUID.randomUUID();
    return Stream.of(
        get(CourseConstants.COURSE_ENDPOINT),
        get(CourseConstants.COURSE_ENDPOINT + "/{id}", id),
        post(CourseConstants.COURSE_ENDPOINT).with(csrf()),
        patch(CourseConstants.COURSE_ENDPOINT + "/{id}", id).with(csrf()),
        delete(CourseConstants.COURSE_ENDPOINT + "/{id}", id).with(csrf()));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"club\": \"Acme Club\", \"course\": \"Acme Course\", \"city\": \"New Town\"}", // Missing
        // state
        "{\"club\": \"Acme Club\", \"course\": \"Acme Course\", \"state\": \"WI\"}", // Missing city
        "{\"club\": \"Acme Club\", \"city\": \"New Town\", \"state\": \"WI\"}", // Missing course
        "{\"course\": \"Acme Course\", \"city\": \"New Town\", \"state\": \"WI\"}", // Missing club
        "{\"club\": \"\", \"course\": \"Acme Course\", \"city\": \"New Town\", \"state\": \"WI\"}", // Blank club
        "{\"club\": \"Acme Club\", \"course\": \"\", \"city\": \"New Town\", \"state\": \"WI\"}", // Blank course
        "{\"club\": \"Acme Club\", \"course\": \"Acme Course\", \"city\": \"\", \"state\": \"WI\"}", // Blank city
      })
  void createCourse_ExpectBadRequest_MissingOrNullFields(String requestBody) throws Exception {
    createCourse(requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(Matchers.matchesPattern("^Field cannot be (null|blank).*")));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"club\": \"Acme Club\", \"course\": \"Acme Course\", \"city\": \"New Town\", \"state\": \"AA\"}", // Invalid state
        "{\"club\": \"Acme Club\", \"course\": \"Acme Course\", \"city\": \"New Town\", \"state\": \"\"}", // Blank state
      })
  void createCourse_ExpectBadRequest_UnsupportedState(String requestBody) throws Exception {
    createCourse(requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(Matchers.matchesPattern("^Invalid state abbreviation:.*")));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        JwtPersona.SCOPE_READ_HANDICAP,
        "",
      })
  void listCoursesWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    listCourses(JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        JwtPersona.SCOPE_READ_HANDICAP,
        "",
      })
  void getCourseWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    getCourse(UUID.randomUUID(), JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
  }

  @Test
  void createCourseWithDefaultScopes_ExpectForbidden() throws Exception {
    String requestBody =
        """
        {"club": "Acme Club", "course": "Acme Course", "city": "Someplace", "state": "MI"}
        """;
    createCourse(requestBody, JwtPersona.forGaryGolfer()).andExpect(status().isForbidden());
  }

  @Test
  void patchCourseWithDefaultScopes_ExpectForbidden() throws Exception {
    String requestBody =
        """
        {"club": "Acme Club"}
        """;
    patchCourse(UUID.randomUUID(), requestBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteCourseWithDefaultScopes_ExpectForbidden() throws Exception {
    deleteCourse(UUID.randomUUID(), JwtPersona.forGaryGolfer()).andExpect(status().isForbidden());
  }
}
