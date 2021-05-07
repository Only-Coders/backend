package tech.onlycoders.backend.dto.auth.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDto {

  @NotBlank(message = "Token is mandatory")
  @Pattern(regexp = "^[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*\\.[A-Za-z0-9-_]*$", message = "Invalid jwt format")
  private String firebaseToken;
}
