package com.alimmit.golf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class DefaultControllerAdvice {

  private static final Logger logger = LoggerFactory.getLogger(DefaultControllerAdvice.class);

  private final MessageSource messageSource;

  DefaultControllerAdvice(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  Object[] constraintValidation(MethodArgumentNotValidException exception) {
    logger.debug("handle MethodArgumentNotValidException | {}", exception.getMessage());
    return exception.getDetailMessageArguments(messageSource, LocaleContextHolder.getLocale());
  }
}
