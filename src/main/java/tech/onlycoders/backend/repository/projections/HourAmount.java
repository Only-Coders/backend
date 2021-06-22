package tech.onlycoders.backend.repository.projections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HourAmount {

  private String hour;
  private Long amount;
}
