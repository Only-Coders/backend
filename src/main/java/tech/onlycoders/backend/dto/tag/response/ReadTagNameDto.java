package tech.onlycoders.backend.dto.tag.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadTagNameDto {

  public String canonicalName;
  public String displayName;
}
