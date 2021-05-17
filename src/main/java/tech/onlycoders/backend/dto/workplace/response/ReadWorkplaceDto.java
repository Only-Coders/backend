package tech.onlycoders.backend.dto.workplace.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadWorkplaceDto {

  private String id;
  private String name;
}
