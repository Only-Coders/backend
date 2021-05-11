package tech.onlycoders.backend.dto.organization.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadOrganizationDto {

  private String id;
  private String name;
}
