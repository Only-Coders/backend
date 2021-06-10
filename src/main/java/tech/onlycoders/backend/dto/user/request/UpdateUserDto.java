package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitProfileDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserDto {

  private Date birthDate;

  private String description;
  private GitProfileDto gitProfile;

  @NotBlank(message = "Firstname is required")
  @NotNull(message = "Firstname is required")
  private String firstName;

  @NotBlank(message = "Lastname is required")
  @NotNull(message = "Lastname is required")
  private String lastName;

  private String imageURI;

  @NotBlank(message = "countryCode is required")
  @NotNull(message = "countryCode is required")
  private String countryCode;
}
