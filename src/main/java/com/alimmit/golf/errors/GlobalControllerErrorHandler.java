package com.alimmit.golf.errors;

import jakarta.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalControllerErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalControllerErrorHandler.class);

  private final MessageSource messageSource;

  public GlobalControllerErrorHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<String> handleNotFound() {
    return ResponseEntity.notFound().build();
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<String> constraintValidation(MethodArgumentNotValidException exception) {
    logger.debug("handle MethodArgumentNotValidException | {}", exception.getMessage());
    String joinedErrors =
        exception.getAllErrors().stream()
            .map(
                e -> {
                  String message;
                  if (e.getCode() != null) {
                    message =
                        messageSource.getMessage(
                            e.getCode(), e.getArguments(), LocaleContextHolder.getLocale());
                  } else {
                    message = e.getDefaultMessage();
                  }
                  return message;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.joining(","));

    return ResponseEntity.badRequest().body(joinedErrors);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<String> constraintViolationException(ConstraintViolationException exception) {
    logger.debug("handle ConstraintViolationException | {}", exception.getMessage());
    String message =
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest().body(message);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<String> httpMessageNotReadableException(
      HttpMessageNotReadableException exception) {
    logger.debug("handle HttpMessageNotReadableException | {}", exception.getMessage());
    String message;
    if (exception.getRootCause() != null) {
      message = exception.getRootCause().getMessage();
    } else {
      message = exception.getCause().getMessage();
    }
    return ResponseEntity.badRequest().body(message);
  }
}
