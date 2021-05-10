package tech.onlycoders.backend.dto.admin.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdminDto {

  private String firstName;
  private String lastName;

  @NotEmpty(message = "Email is required.")
  @Email(message = "Email must be valid.")
  private String email;

  private String imageURI;
}
