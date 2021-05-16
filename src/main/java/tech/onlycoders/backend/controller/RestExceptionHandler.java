package tech.onlycoders.backend.controller;

import static org.springframework.http.HttpStatus.*;

import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.ApiValidationError;
import tech.onlycoders.backend.exception.ApiException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(ApiException.class)
  protected ResponseEntity<ApiErrorResponse> handleApiError(ApiException apiException) {
    return ResponseEntity
      .status(apiException.getStatus())
      .body(new ApiErrorResponse(apiException.getStatus(), apiException.getError()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity<ApiErrorResponse> handleEntityNotFound(AccessDeniedException ex) {
    return ResponseEntity.status(FORBIDDEN).body(new ApiErrorResponse(FORBIDDEN, "Forbidden"));
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiErrorResponse> handleException(MethodArgumentNotValidException exception) {
    var errorMessages = exception
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(
        err ->
          ApiValidationError
            .builder()
            .field(err.getField())
            .message(err.getDefaultMessage())
            .rejectedValue(err.getRejectedValue())
            .build()
      )
      .distinct()
      .collect(Collectors.toList());
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(new ApiErrorResponse(BAD_REQUEST, "Validation Error", errorMessages));
  }
}
