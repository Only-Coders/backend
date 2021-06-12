package tech.onlycoders.backend.dto.datareport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValueDto {

  private String attribute;

  private Integer value;
}
