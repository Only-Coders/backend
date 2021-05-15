package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkExperienceDto {

  @NotNull(message = "Nombre de empresa requerido.")
  @NotBlank(message = "Nombre de empresa requerido.")
  private String name;

  private String id;

  private String position;

  @NotNull
  private Date since;

  private Date until;
}
