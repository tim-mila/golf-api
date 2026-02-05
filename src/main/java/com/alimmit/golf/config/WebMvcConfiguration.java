package com.alimmit.golf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
class WebMvcConfiguration {

  @Bean
  Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder()
        .modules(new JavaTimeModule())
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .featuresToDisable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
