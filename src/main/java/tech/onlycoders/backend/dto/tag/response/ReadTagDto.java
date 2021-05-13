package tech.onlycoders.backend.dto.tag.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadTagDto {

  private String name;
  private String canonicalName;
}
