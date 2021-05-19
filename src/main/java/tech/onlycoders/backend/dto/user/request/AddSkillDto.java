package tech.onlycoders.backend.dto.user.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddSkillDto {

  @NotNull(message = "Name is required.")
  @NotBlank(message = "Name is required.")
  private String name;

  private String canonicalName;
}
