package tech.onlycoders.backend.dto.user.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCountryDto {

  @NotBlank(message = "Code is required.")
  @NotNull(message = "Code is required.")
  private String code;
}
