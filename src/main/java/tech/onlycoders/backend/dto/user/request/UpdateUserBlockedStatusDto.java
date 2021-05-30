package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitProfileDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserBlockedStatusDto {

  @NotNull(message = "Blocked is required")
  private Boolean blocked;
}
