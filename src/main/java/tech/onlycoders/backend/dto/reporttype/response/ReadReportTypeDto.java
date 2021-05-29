package tech.onlycoders.backend.dto.reporttype.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadReportTypeDto {

  private String id;
  private String name;
}
