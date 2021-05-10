package tech.onlycoders.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserLiteDto {

  public String canonicalName;
  public String firstName;
  public String lastName;
  public String imageURI;
}
