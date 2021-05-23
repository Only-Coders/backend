package tech.onlycoders.backend.dto.post.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.model.ReactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionQuantityDto {

  private ReactionType reaction;
  private Long quantity;
}
