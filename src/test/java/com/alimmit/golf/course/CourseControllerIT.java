package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtClaimApplier;
import com.alimmit.golf.utils.JwtPersona;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class CourseControllerIT extends AbstractCourseControllerTest {

  private static final String REQUEST_BODY =
      """
      {"club": "Test Club", "course": "Test Course", "city": "Test City", "state": "WI"}
      """;

  private final ObjectMapper objectMapper;

  @Autowired
  CourseControllerIT(MockMvc mockMvc, ObjectMapper objectMapper) {
    super(mockMvc);
    this.objectMapper = objectMapper;
  }

  @Test
  void addCourse() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());
  }

  @Test
  void addDuplicateCourse_ExpectConflict() throws Exception {
    createCourse(REQUEST_BODY, JwtPersona.forGaryGolfer(), status().isCreated());
    createCourse(REQUEST_BODY, JwtPersona.forGaryGolfer(), status().isConflict());
  }

  @Test
  void addSameCourseForDifferentUsers_ExpectCreated() throws Exception {
    createCourse(REQUEST_BODY, JwtPersona.forGaryGolfer(), status().isCreated());
    createCourse(REQUEST_BODY, JwtPersona.forPatPutter(), status().isCreated());
  }

  @Test
  void patchCourseToMatchExisting_ExpectConflict() throws Exception {
    // Create two distinct courses for Gary
    createCourse(REQUEST_BODY, JwtPersona.forGaryGolfer(), status().isCreated());
    String secondBody =
        """
        {"club": "Other Club", "course": "Other Course", "city": "Other City", "state": "MI"}
        """;
    String secondResponse =
        createCourse(secondBody, JwtPersona.forGaryGolfer(), status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID secondId = UUID.fromString(objectMapper.readTree(secondResponse).at("/courseId").asText());

    // Patching the second course to match the first should conflict
    String patchBody =
        """
        {"club": "Test Club", "course": "Test Course", "city": "Test City", "state": "WI"}
        """;
    patchCourse(secondId, patchBody, JwtPersona.forGaryGolfer(), status().isConflict());
  }

  @Test
  void patchCourseToSameValues_ExpectOk() throws Exception {
    // Patching a course with its own values should not conflict with itself
    String responseBody =
        createAndAssertCourse(JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID courseId = UUID.fromString(objectMapper.readTree(responseBody).at("/courseId").asText());

    patchCourse(courseId, REQUEST_BODY, JwtPersona.forGaryGolfer(), status().isOk());
  }

  @Test
  void canOnlyListMyCourses() throws Exception {
    // Create course for Gary Golfer
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    // Gary sees his course
    listCourses(JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    // Pat sees nothing
    listCourses(JwtPersona.forPatPutter(), status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void canOnlyFetchMyCourses() throws Exception {
    String responseBody =
        createAndAssertCourse(JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID courseId = UUID.fromString(objectMapper.readTree(responseBody).at("/courseId").asText());
    assertThat(courseId).isNotNull();

    // Gary can fetch his course
    getCourse(courseId, JwtPersona.forGaryGolfer(), status().isOk());

    // Pat gets not found
    getCourse(courseId, JwtPersona.forPatPutter(), status().isNotFound());
  }

  @Test
  void canOnlyDeleteMyCourses() throws Exception {
    String responseBody =
        createAndAssertCourse(JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID courseId = UUID.fromString(objectMapper.readTree(responseBody).at("/courseId").asText());
    assertThat(courseId).isNotNull();

    // Pat cannot delete Gary's course
    deleteCourse(courseId, JwtPersona.forPatPutter(), status().isNotFound());

    // Gary can delete his own course
    deleteCourse(courseId, JwtPersona.forGaryGolfer(), status().isNoContent());

    // Gary gets not found on second attempt
    deleteCourse(courseId, JwtPersona.forGaryGolfer(), status().isNotFound());
  }

  @Test
  void searchCourses_ExactWord() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    searchCourses("Test", JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$.[0].club").value("Test Club"));
  }

  @Test
  void searchCourses_PartialWord() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    searchCourses("Tes", JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$.[0].club").value("Test Club"));
  }

  @Test
  void searchCourses_MultiWord() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    searchCourses("Test Club", JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$.[0].club").value("Test Club"));
  }

  @Test
  void searchCourses_SpecialCharsOnly_ReturnsEmpty() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    searchCourses("&|!", JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void searchCourses_MultiTenancy() throws Exception {
    createAndAssertCourse(JwtPersona.forGaryGolfer());

    // Gary finds his course
    searchCourses("Test Club", JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    // Pat finds nothing
    searchCourses("Test Club", JwtPersona.forPatPutter(), status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  private ResultActions createAndAssertCourse(JwtClaimApplier fn) throws Exception {
    return createCourse(REQUEST_BODY, fn, status().isCreated())
        .andExpectAll(
            jsonPath("$.courseId").exists(),
            jsonPath("$.club").value("Test Club"),
            jsonPath("$.course").value("Test Course"),
            jsonPath("$.city").value("Test City"),
            jsonPath("$.state").value("WI"),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }
}
