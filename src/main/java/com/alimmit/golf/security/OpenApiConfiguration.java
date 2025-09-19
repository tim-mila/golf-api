package com.alimmit.golf.security;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Golf API", summary = "Golf tracking API",
        description = "Golf course lookup, scorecard, and handicap index tracking", version = "v1"),
    servers = {@Server(url = "http://localhost:8080", description = "Localhost")},
    security = {@SecurityRequirement(name = "OAUTH2")})
@SecurityScheme(name = "OAUTH2", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(authorizationCode = @OAuthFlow(
        authorizationUrl = "https://dev-ccm6yq3t150kr7mm.us.auth0.com/oauth/authorize",
        tokenUrl = "https://dev-ccm6yq3t150kr7mm.us.auth0.com/oauth/token")))
class OpenApiConfiguration {

}
