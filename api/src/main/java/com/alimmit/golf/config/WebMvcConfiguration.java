package com.alimmit.golf.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration
class WebMvcConfiguration {

  @Bean
  JsonMapperBuilderCustomizer jackson2ObjectMapperBuilder() {

    return jsonMapperBuilder ->
        jsonMapperBuilder
            .changeDefaultPropertyInclusion(
                incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
