package com.alimmit.golf.errors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerErrorHandler {

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<String> handleNotFound() {
    return ResponseEntity.notFound().build();
  }
}
