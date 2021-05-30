package tech.onlycoders.backend.dto.user.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserToDeleteDto {

  private Date eliminationDate;
}
