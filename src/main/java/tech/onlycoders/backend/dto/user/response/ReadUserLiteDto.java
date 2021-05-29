package tech.onlycoders.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserLiteDto {

  public String canonicalName;
  public String firstName;
  public String lastName;
  public String imageURI;
  public Integer amountOfMedals;
  public ReadWorkPositionDto currentPosition;
}
