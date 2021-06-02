package tech.onlycoders.backend.dto.user.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserBlockedStatusDto {

  @NotNull(message = "Blocked is required")
  private Boolean blocked;
}
