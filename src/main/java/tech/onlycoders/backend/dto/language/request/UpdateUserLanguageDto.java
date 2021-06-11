package tech.onlycoders.backend.dto.language.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateUserLanguageDto {

  @NotNull(message = "code is mandatory")
  @NotBlank(message = "code is mandatory")
  private String code;
}
