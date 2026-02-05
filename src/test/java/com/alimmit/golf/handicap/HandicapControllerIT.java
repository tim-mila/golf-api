package com.alimmit.golf.handicap;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtPersona;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "handicap-before.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class HandicapControllerIT extends AbstractHandicapControllerMockMvc {

  @Autowired
  HandicapControllerIT(MockMvc mockMvc) {
    super(mockMvc);
  }

  @Test
  void testGetHandicap() throws Exception {
    getMyHandicap(JwtPersona::forGaryGolfer)
        .andExpect(status().isOk())
        .andExpectAll(
            jsonPath("$.handicapId").value("hdcp-5678"),
            jsonPath("$.golferId").value(JwtPersona.GARY_GOLFER.sub()),
            jsonPath("$.handicapIndex").value(6.1),
            jsonPath("$.roundsUsed").value(4),
            jsonPath("$.totalRounds").value(4),
            jsonPath("$.createdAt").exists()
        );
  }

  @Test
  void testGetHandicap_ExpectMissing() throws Exception {
    getMyHandicap(JwtPersona::forPatPutter)
        .andExpect(status().isNotFound());
  }
}