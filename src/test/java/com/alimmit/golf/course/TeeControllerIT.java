package com.alimmit.golf.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtClaimApplier;
import com.alimmit.golf.utils.JwtPersona;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

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
  void addTee() throws Exception {
    UUID courseId = createCourse();
    createAndAssertTee(courseId, new TeeStub("Black", 72, 128.3, 71.9), JwtPersona.forGaryGolfer());
  }

  @Test
  @WithMockUser("123")
  void canOnlyListMyTees() throws Exception {
    UUID courseId = createCourse();
    createAndAssertTee(courseId, TeeStub.defaultStub(), JwtPersona.forGaryGolfer());

    // Gary sees his tee
    listTeesByCourse(courseId, JwtPersona.forGaryGolfer(), status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    // Pat sees nothing — course not owned by Pat
    listTeesByCourse(courseId, JwtPersona.forPatPutter(), status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @WithMockUser("123")
  void canOnlyFetchMyTees() throws Exception {
    UUID courseId = createCourse();
    String responseBody =
        createAndAssertTee(courseId, TeeStub.defaultStub(), JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(responseBody).at("/teeId").asString());
    assertThat(teeId).isNotNull();

    // Gary can fetch his tee
    getTee(teeId, JwtPersona.forGaryGolfer(), status().isOk());

    // Pat gets not found
    getTee(teeId, JwtPersona.forPatPutter(), status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void canOnlyDeleteMyTees() throws Exception {
    UUID courseId = createCourse();
    String responseBody =
        createAndAssertTee(courseId, TeeStub.defaultStub(), JwtPersona.forGaryGolfer())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(responseBody).at("/teeId").asString());
    assertThat(teeId).isNotNull();

    // Pat cannot delete Gary's tee
    deleteTee(teeId, JwtPersona.forPatPutter(), status().isNotFound());

    // Gary can delete his own tee
    deleteTee(teeId, JwtPersona.forGaryGolfer(), status().isNoContent());

    // Gary gets not found on second attempt
    deleteTee(teeId, JwtPersona.forGaryGolfer(), status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void createAndGetTee() throws Exception {
    UUID courseId = createCourse();

    String requestBody =
        """
        {"name": "Blue", "par": 72, "slope": 131.0, "rating": 71.2}
        """;

    String responseBody =
        createTee(courseId, requestBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andExpectAll(
                jsonPath("$.courseId").value(courseId.toString()),
                jsonPath("$.name").value("Blue"),
                jsonPath("$.slope").value(131.0),
                jsonPath("$.rating").value(71.2),
                jsonPath("$.teeId").exists(),
                jsonPath("$.createdAt").exists(),
                jsonPath("$.lastModifiedAt").exists())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(responseBody).at("/teeId").asString());

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
        {"name": "Blue", "par": 72, "slope": 131.0, "rating": 71.2}
        """;

    createTee(UUID.randomUUID(), requestBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser("123")
  void listTeesByCourse() throws Exception {
    UUID courseId = createCourse();
    createAndAssertTee(courseId, new TeeStub("Blue", 72, 123.2, 71.5), JwtPersona.forGaryGolfer());
    createAndAssertTee(courseId, new TeeStub("White", 72, 121.2, 71.0), JwtPersona.forGaryGolfer());

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
        {"name": "Blue", "par": 72, "slope": 131.0, "rating": 71.2}
        """;

    String createResponse =
        createTee(courseId, createBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(createResponse).at("/teeId").asString());

    String patchBody =
        """
        {"name": "White", "par": 72, "slope": 125.0, "rating": 69.5}
        """;

    patchTee(teeId, patchBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.teeId").value(teeId.toString()),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.name").value("White"),
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
        {"name": "Blue", "par": 72, "slope": 131.0, "rating": 71.2}
        """;

    String createResponse =
        createTee(courseId, createBody, JwtPersona.forGaryGolfer())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID teeId = UUID.fromString(objectMapper.readTree(createResponse).at("/teeId").asString());

    deleteTee(teeId, JwtPersona.forGaryGolfer()).andExpect(status().isNoContent());

    getTee(teeId, JwtPersona.forGaryGolfer()).andExpect(status().isNotFound());
  }

  private ResultActions createAndAssertTee(UUID courseId, TeeStub stub, JwtClaimApplier fn)
      throws Exception {

    return createTee(courseId, stub.asRequestBody(), fn, status().isCreated())
        .andExpectAll(
            jsonPath("$.teeId").exists(),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.name").value(stub.name),
            jsonPath("$.par").value(stub.par),
            jsonPath("$.slope").value(stub.slope),
            jsonPath("$.rating").value(stub.rating),
            jsonPath("$.createdAt").isString(),
            jsonPath("$.lastModifiedAt").isString());
  }

  private UUID createCourse() {
    return courseService
        .create(new CreateCourseRequest("Test Club", "Test Course", "Test City", USState.WISCONSIN))
        .courseId();
  }

  private record TeeStub(String name, int par, double slope, double rating) {

    String asRequestBody() {
      return """
        {"name": "%s", "par": %d, "slope": %.1f, "rating": %.1f}
        """
          .formatted(name, par, slope, rating);
    }

    static TeeStub defaultStub() {
      return new TeeStub("Blue", 72, 123.4, 71.3);
    }
  }
}
