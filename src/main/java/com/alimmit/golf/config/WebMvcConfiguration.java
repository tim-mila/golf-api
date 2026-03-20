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

    return jsonMapperBuilder -> {
      jsonMapperBuilder.disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
      jsonMapperBuilder.changeDefaultPropertyInclusion(
          incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL));
    };
  }
}
