package tech.onlycoders.backend.dto.contactrequest.response;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadContactRequestDto {

  private String id;

  private ReadUserLiteDto requester;

  private String message;
}
