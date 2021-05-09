package tech.onlycoders.backend.dto.person.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadPersonLiteDto {

  public String canonicalName;
  public String firstName;
  public String lastName;
  public String imageUrl;
}
