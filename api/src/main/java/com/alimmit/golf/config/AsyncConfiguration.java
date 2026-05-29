package com.alimmit.golf.config;

import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
class AsyncConfiguration {

  @Bean
  Executor scorecardEventListenerExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setTaskDecorator(
        runnable -> {
          Map<String, String> mdc = MDC.getCopyOfContextMap();
          return () -> {
            try {
              if (mdc != null) {
                MDC.setContextMap(mdc);
              }
              runnable.run();
            } finally {
              MDC.clear();
            }
          };
        });
    executor.initialize();
    return executor;
  }
}
