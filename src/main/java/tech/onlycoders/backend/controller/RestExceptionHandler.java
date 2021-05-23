package tech.onlycoders.backend.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.ApiValidationError;
import tech.onlycoders.backend.exception.ApiException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

  private final MessageSource msgSrc;

  public RestExceptionHandler(MessageSource msgSrc) {
    this.msgSrc = msgSrc;
  }

  private String getMessage(String language, String errorCode) {
    Locale locale;
    locale = new Locale(Objects.requireNonNullElse(language, "en"));
    return msgSrc.getMessage(errorCode, null, locale);
  }

  @ExceptionHandler(ApiException.class)
  protected ResponseEntity<ApiErrorResponse> handleApiError(ApiException apiException, WebRequest request) {
    var message = this.getMessage(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE), apiException.getError());
    return ResponseEntity
      .status(apiException.getStatus())
      .body(new ApiErrorResponse(apiException.getStatus(), message));
  }

  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity<ApiErrorResponse> handleEntityNotFound(AccessDeniedException ex, WebRequest request) {
    var message = this.getMessage(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE), "error.forbidden");
    return ResponseEntity.status(FORBIDDEN).body(new ApiErrorResponse(FORBIDDEN, message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleException(MethodArgumentNotValidException exception) {
    var errorMessages = exception
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(
        fieldError ->
          ApiValidationError
            .builder()
            .field(fieldError.getField())
            .message(fieldError.getDefaultMessage())
            .rejectedValue(fieldError.getRejectedValue())
            .build()
      )
      .collect(Collectors.toList());
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(new ApiErrorResponse(BAD_REQUEST, "Validation Error", errorMessages));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleException(ConstraintViolationException exception) {
    var errorMessages = exception
      .getConstraintViolations()
      .stream()
      .map(
        err ->
          ApiValidationError
            .builder()
            .field(err.getPropertyPath().toString())
            .message(err.getMessage())
            .rejectedValue(err.getInvalidValue())
            .build()
      )
      .collect(Collectors.toList());
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(new ApiErrorResponse(BAD_REQUEST, "Validation Error", errorMessages));
  }
}
