package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitPlatform;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {

  private Date birthDate;
  private String gitProfileURI;
  private GitPlatform gitPlatform;
  private String description;

  @NotBlank(message = "Firstname is required")
  @NotNull(message = "Firstname is required")
  private String firstName;

  private String lastName;
  private String imageURI;
}
