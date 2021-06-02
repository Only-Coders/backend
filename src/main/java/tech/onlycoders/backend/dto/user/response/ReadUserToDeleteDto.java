package tech.onlycoders.backend.dto.user.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserToDeleteDto {

  private Date eliminationDate;
}
