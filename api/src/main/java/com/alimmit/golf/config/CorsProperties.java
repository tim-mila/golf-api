package com.alimmit.golf.config;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cors")
@Validated
record CorsProperties(
    @NotEmpty List<String> allowedOrigins,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    long maxAge) {}
