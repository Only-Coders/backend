package tech.onlycoders.backend.dto.user.request;

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkExperienceDto {

  @NotNull
  private Date since;

  private Date until;
}
