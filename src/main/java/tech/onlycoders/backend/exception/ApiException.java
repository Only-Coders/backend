package tech.onlycoders.backend.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiException extends Exception {

  private static final long serialVersionUID = 1L;
  private HttpStatus status;
  private String error;

  public ApiException(HttpStatus status, String error) {
    this.status = status;
    this.error = error;
  }
}
