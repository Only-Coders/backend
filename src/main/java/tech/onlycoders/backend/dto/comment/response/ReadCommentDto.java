package tech.onlycoders.backend.dto.comment.response;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.ReactionQuantityDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.model.ReactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadCommentDto {

  private String Id;
  private ReadUserLiteDto publisher;
  private String message;
  private List<ReactionQuantityDto> reactions;
  private ReactionType myReaction;
  private Date createdAt;
}
