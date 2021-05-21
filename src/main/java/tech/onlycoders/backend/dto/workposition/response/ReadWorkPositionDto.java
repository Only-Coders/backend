package tech.onlycoders.backend.dto.workposition.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadWorkPositionDto {

  private ReadWorkplaceDto workplace;

  private Date since;

  private Date until;

  private String position;
}
