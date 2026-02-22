package com.alimmit.golf.course;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtPersona;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class TeeControllerIT extends AbstractTeeControllerTest {

  private final CourseService courseService;
  private final ObjectMapper objectMapper;

  @Autowired
  TeeControllerIT(MockMvc mockMvc, CourseService courseService, ObjectMapper objectMapper) {
    super(mockMvc);
    this.courseService = courseService;
    this.objectMapper = objectMapper;
  }

  @Test
  @WithMockUser("123")
  void createAndGetTee() throws Exception {
    UUID courseId = createCourse();

    String requestBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    String responseBody =
        createTee(courseId, requestBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andExpectAll(
                jsonPath("$.courseId").value(courseId.toString()),
                jsonPath("$.name").value("Blue"),
                jsonPath("$.yardage").value(6500),
                jsonPath("$.slope").value(131.0),
                jsonPath("$.rating").value(71.2),
                jsonPath("$.teeId").exists(),
                jsonPath("$.createdAt").exists(),
                jsonPath("$.lastModifiedAt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(responseBody).at("/teeId").asText());

    getTee(teeId, JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.teeId").value(teeId.toString()),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.name").value("Blue"));
  }

  @Test
  @WithMockUser("123")
  void createTee_courseNotFound() throws Exception {
    String requestBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    createTee(UUID.randomUUID(), requestBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void listTeesByCourse() throws Exception {
    UUID courseId = createCourse();
    createTeeViaService(courseId, "Blue");
    createTeeViaService(courseId, "White");

    listTeesByCourse(courseId, JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @WithMockUser("123")
  void listTeesByCourse_empty() throws Exception {
    UUID courseId = createCourse();

    listTeesByCourse(courseId, JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getTee_notFound() throws Exception {
    getTee(UUID.randomUUID(), JwtPersona.forGaryGolfer()).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void patchTee() throws Exception {
    UUID courseId = createCourse();

    String createBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    String createResponse =
        createTee(courseId, createBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(createResponse).at("/teeId").asText());

    String patchBody =
        """
        {"name": "White", "par": 72, "yardage": 6100, "slope": 125.0, "rating": 69.5}
        """;

    patchTee(teeId, patchBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.teeId").value(teeId.toString()),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.name").value("White"),
            jsonPath("$.yardage").value(6100),
            jsonPath("$.slope").value(125.0),
            jsonPath("$.rating").value(69.5));
  }

  @Test
  void patchTee_notFound() throws Exception {
    String patchBody =
        """
        {"name": "White"}
        """;

    patchTee(UUID.randomUUID(), patchBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void deleteTee() throws Exception {
    UUID courseId = createCourse();

    String createBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    String createResponse =
        createTee(courseId, createBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(createResponse).at("/teeId").asText());

    deleteTee(teeId, JwtPersona.forGaryGolfer()).andExpect(status().isNoContent());

    getTee(teeId, JwtPersona.forGaryGolfer()).andExpect(status().isNotFound());
  }

  private UUID createCourse() {
    return courseService
        .create(new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN))
        .courseId();
  }

  private void createTeeViaService(UUID courseId, String name) {
    courseService.get(courseId).orElseThrow();
    // Use the controller's create endpoint for consistency
    try {
      createTee(
          courseId,
          """
          {"name": "%s", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
          """
              .formatted(name),
          JwtPersona.forGaryGolfer());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
