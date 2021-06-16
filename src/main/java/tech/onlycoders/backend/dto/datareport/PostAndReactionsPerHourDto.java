package tech.onlycoders.backend.dto.datareport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAndReactionsPerHourDto {

  private String hour;

  private Long posts;

  private Long reactions;
}
