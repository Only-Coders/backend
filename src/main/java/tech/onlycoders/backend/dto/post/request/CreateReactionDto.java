package tech.onlycoders.backend.dto.post.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.model.ReactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReactionDto {

  @NotNull(message = "Reaction is mandatory")
  private ReactionType reactionType;
}
