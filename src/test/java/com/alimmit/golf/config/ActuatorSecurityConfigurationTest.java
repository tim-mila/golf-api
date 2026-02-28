package com.alimmit.golf.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alimmit.golf.utils.JwtPersona;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  @ParameterizedTest
  @ValueSource(strings = {"/actuator/health", "/actuator/info"})
  void unauthenticated_expectUnauthorized(String path) throws Exception {
    mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/actuator/health", "/actuator/info"})
  void withDefaultScopes_expectForbidden(String path) throws Exception {
    mockMvc
        .perform(get(path).with(jwt().jwt(JwtPersona.forGaryGolfer()::apply)))
        .andExpect(status().isForbidden());
  }

  @Test
  void amyAdmin_getHealth_expectOk() throws Exception {
    mockMvc
        .perform(get("/actuator/health").with(jwt().jwt(JwtPersona.forAmyAdmin()::apply)))
        .andExpect(status().isOk());
  }

  @Test
  void amyAdmin_getInfo_expectOk() throws Exception {
    mockMvc
        .perform(get("/actuator/info").with(jwt().jwt(JwtPersona.forAmyAdmin()::apply)))
        .andExpect(status().isOk());
  }
}
