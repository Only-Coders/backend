package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitPlatform;
import tech.onlycoders.backend.dto.user.GitProfileDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto {

  private Date birthDate;
  private String description;
  private GitProfileDto gitProfile;

  @NotBlank(message = "Firstname is required")
  @NotNull(message = "Firstname is required")
  private String firstName;

  private String lastName;
  private String imageURI;
}
