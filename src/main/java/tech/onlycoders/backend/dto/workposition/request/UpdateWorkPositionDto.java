package tech.onlycoders.backend.dto.workposition.request;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWorkPositionDto {

  @NotBlank
  private String position;

  @NotNull
  private Date since;

  private Date until;
}
