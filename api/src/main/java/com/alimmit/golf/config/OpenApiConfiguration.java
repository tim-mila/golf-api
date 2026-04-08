package com.alimmit.golf.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
class OpenApiConfiguration {

  private final OAuth2ResourceServerProperties oauth2Properties;

  OpenApiConfiguration(OAuth2ResourceServerProperties oauth2Properties) {
    this.oauth2Properties = oauth2Properties;
  }

  @Bean
  OpenAPI openAPI() {
    String issuerUri = oauth2Properties.getJwt().getIssuerUri();
    String base = Objects.requireNonNull(issuerUri).endsWith("/") ? issuerUri : issuerUri + "/";
    return new OpenAPI()
        .info(
            new Info()
                .title("Golf API")
                .summary("Golf tracking API")
                .description("Golf course lookup, scorecard, and handicap index tracking")
                .version("v1"))
        .addServersItem(new Server().url("http://localhost:8080").description("Localhost"))
        .addSecurityItem(new SecurityRequirement().addList("OAUTH2"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "OAUTH2",
                    new SecurityScheme()
                        .name("OAUTH2")
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(
                            new OAuthFlows()
                                .authorizationCode(
                                    new OAuthFlow()
                                        .authorizationUrl(base + "oauth/authorize")
                                        .tokenUrl(base + "oauth/token")))));
  }
}
