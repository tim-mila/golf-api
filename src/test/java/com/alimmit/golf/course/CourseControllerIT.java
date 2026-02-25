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
