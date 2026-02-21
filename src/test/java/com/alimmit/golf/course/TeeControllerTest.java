package com.alimmit.golf.course;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.alimmit.golf.config.MethodSecurityConfiguration;
import com.alimmit.golf.utils.JwtPersona;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(TeeController.class)
@Import(MethodSecurityConfiguration.class)
class TeeControllerTest extends AbstractTeeControllerTest {

  @MockitoBean private TeeService teeService;

  @Autowired
  public TeeControllerTest(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void listTeesByCourse_ExpectOk() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(teeService.listByCourse(courseId))
        .thenReturn(
            List.of(
                new TeeDto(
                    UUID.randomUUID(),
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "Blue",
                    72,
                    6500,
                    new BigDecimal("131.0"),
                    new BigDecimal("71.2")),
                new TeeDto(
                    UUID.randomUUID(),
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "White",
                    72,
                    6100,
                    new BigDecimal("125.0"),
                    new BigDecimal("69.5"))));

    listTeesByCourse(courseId, JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpectAll(
            jsonPath("$.[0].name").value("Blue"),
            jsonPath("$.[0].yardage").value(6500),
            jsonPath("$.[0].slope").value(131.0),
            jsonPath("$.[0].rating").value(71.2),
            jsonPath("$.[1].name").value("White"),
            jsonPath("$.[1].yardage").value(6100),
            jsonPath("$.[1].slope").value(125.0),
            jsonPath("$.[1].rating").value(69.5));
  }

  @Test
  void listTeesByCourse_ExpectEmpty() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(teeService.listByCourse(courseId)).thenReturn(List.of());

    listTeesByCourse(courseId, JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void getTee_ExpectOk() throws Exception {
    UUID teeId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    when(teeService.get(teeId))
        .thenReturn(
            Optional.of(
                new TeeDto(
                    teeId,
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "Blue",
                    72,
                    6500,
                    new BigDecimal("131.0"),
                    new BigDecimal("71.2"))));

    getTee(teeId, JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.teeId").value(teeId.toString()),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.name").value("Blue"),
            jsonPath("$.yardage").value(6500),
            jsonPath("$.slope").value(131.0),
            jsonPath("$.rating").value(71.2));
  }

  @Test
  void getTee_ExpectNotFound() throws Exception {
    UUID teeId = UUID.randomUUID();
    when(teeService.get(teeId)).thenReturn(Optional.empty());

    getTee(teeId, JwtPersona.forAmyAdmin()).andExpect(status().isNotFound());
  }

  @Test
  void createTee_ExpectCreated() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(teeService.create(
            eq(courseId),
            argThat(
                m ->
                    m.name().equals("Blue")
                        && m.yardage().equals(6500)
                        && m.slope().compareTo(new BigDecimal("131.0")) == 0
                        && m.rating().compareTo(new BigDecimal("71.2")) == 0)))
        .thenReturn(
            Optional.of(
                new TeeDto(
                    UUID.randomUUID(),
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "Blue",
                    72,
                    6500,
                    new BigDecimal("131.0"),
                    new BigDecimal("71.2"))));

    String requestBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    createTee(courseId, requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isCreated())
        .andExpectAll(
            jsonPath("$.name").value("Blue"),
            jsonPath("$.yardage").value(6500),
            jsonPath("$.slope").value(131.0),
            jsonPath("$.rating").value(71.2),
            jsonPath("$.courseId").value(courseId.toString()),
            jsonPath("$.teeId").exists(),
            jsonPath("$.createdAt").exists(),
            jsonPath("$.lastModifiedAt").exists());
  }

  @Test
  void createTee_CourseNotFound_ExpectNotFound() throws Exception {
    UUID courseId = UUID.randomUUID();
    when(teeService.create(eq(courseId), argThat(m -> m.name().equals("Blue"))))
        .thenReturn(Optional.empty());

    String requestBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;

    createTee(courseId, requestBody, JwtPersona.forAmyAdmin()).andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"par\": 72, \"yardage\": 6500, \"slope\": 131.0, \"rating\": 71.2}",
        "{\"name\": \"Blue\", \"par\": 72, \"slope\": 131.0, \"rating\": 71.2}",
        "{\"name\": \"Blue\", \"par\": 72, \"yardage\": 6500, \"rating\": 71.2}",
        "{\"name\": \"Blue\", \"par\": 72, \"yardage\": 6500, \"slope\": 131.0}",
        "{\"name\": \"\", \"par\": 72, \"yardage\": 6500, \"slope\": 131.0, \"rating\": 71.2}",
        "{\"name\": \"Blue\", \"yardage\": 6500, \"slope\": 131.0, \"rating\": 71.2}",
      })
  void createTee_ExpectBadRequest(String requestBody) throws Exception {
    createTee(UUID.randomUUID(), requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchTee_ExpectOk() throws Exception {
    UUID teeId = UUID.randomUUID();
    UUID courseId = UUID.randomUUID();
    when(teeService.patch(eq(teeId), argThat(m -> m.name().orElseThrow().equals("White"))))
        .thenReturn(
            Optional.of(
                new TeeDto(
                    teeId,
                    courseId,
                    Instant.now(),
                    Instant.now(),
                    "White",
                    72,
                    6100,
                    new BigDecimal("125.0"),
                    new BigDecimal("69.5"))));

    String requestBody =
        """
        {"name": "White", "par": 72, "yardage": 6100, "slope": 125.0, "rating": 69.5}
        """;

    patchTee(teeId, requestBody, JwtPersona.forAmyAdmin())
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.name").value("White"),
            jsonPath("$.yardage").value(6100),
            jsonPath("$.slope").value(125.0),
            jsonPath("$.rating").value(69.5));
  }

  @Test
  void patchTee_NotFound() throws Exception {
    UUID teeId = UUID.randomUUID();
    when(teeService.patch(eq(teeId), argThat(m -> m.name().orElseThrow().equals("White"))))
        .thenReturn(Optional.empty());

    String requestBody =
        """
        {"name": "White"}
        """;

    patchTee(teeId, requestBody, JwtPersona.forAmyAdmin()).andExpect(status().isNotFound());
  }

  @Test
  void deleteTee_ExpectNoContent() throws Exception {
    UUID teeId = UUID.randomUUID();
    doNothing().when(teeService).delete(teeId);
    deleteTee(teeId, JwtPersona.forAmyAdmin()).andExpect(status().isNoContent());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        JwtPersona.SCOPE_READ_HANDICAP,
        "",
      })
  void listTeesWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    listTeesByCourse(UUID.randomUUID(), JwtPersona.forGaryGolfer(scope))
        .andExpect(status().isForbidden());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        JwtPersona.SCOPE_READ_SCORECARD,
        JwtPersona.SCOPE_WRITE_SCORECARD,
        JwtPersona.SCOPE_READ_HANDICAP,
        "",
      })
  void getTeeWithDisallowedScopes_ExpectForbidden(String scope) throws Exception {
    getTee(UUID.randomUUID(), JwtPersona.forGaryGolfer(scope)).andExpect(status().isForbidden());
  }

  @Test
  void createTeeWithDefaultScopes_ExpectForbidden() throws Exception {
    String requestBody =
        """
        {"name": "Blue", "par": 72, "yardage": 6500, "slope": 131.0, "rating": 71.2}
        """;
    createTee(UUID.randomUUID(), requestBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isForbidden());
  }

  @Test
  void patchTeeWithDefaultScopes_ExpectForbidden() throws Exception {
    String requestBody =
        """
        {"name": "White"}
        """;
    patchTee(UUID.randomUUID(), requestBody, JwtPersona.forGaryGolfer())
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteTeeWithDefaultScopes_ExpectForbidden() throws Exception {
    deleteTee(UUID.randomUUID(), JwtPersona.forGaryGolfer()).andExpect(status().isForbidden());
  }
}
