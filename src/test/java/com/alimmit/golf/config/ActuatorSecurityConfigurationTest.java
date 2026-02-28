package com.alimmit.golf.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtPersona;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest(properties = "management.server.port=")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ActuatorSecurityConfigurationTest {

  @Autowired private MockMvc mockMvc;

  // --- /actuator/health is public ---

  @Test
  void getHealth_unauthenticated_expectOk() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void getHealth_withDefaultScopes_expectOk() throws Exception {
    mockMvc
        .perform(get("/actuator/health").with(jwt().jwt(JwtPersona.forGaryGolfer()::apply)))
        .andExpect(status().isOk());
  }

  // --- /actuator/info requires read:actuator ---

  @Test
  void getInfo_unauthenticated_expectUnauthorized() throws Exception {
    mockMvc.perform(get("/actuator/info")).andExpect(status().isUnauthorized());
  }

  @Test
  void getInfo_withDefaultScopes_expectForbidden() throws Exception {
    mockMvc
        .perform(get("/actuator/info").with(jwt().jwt(JwtPersona.forGaryGolfer()::apply)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getInfo_amyAdmin_expectOk() throws Exception {
    mockMvc
        .perform(get("/actuator/info").with(jwt().jwt(JwtPersona.forAmyAdmin()::apply)))
        .andExpect(status().isOk());
  }
}
