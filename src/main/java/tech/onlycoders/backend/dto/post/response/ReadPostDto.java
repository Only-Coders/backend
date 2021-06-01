package tech.onlycoders.backend.dto.post.response;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.ReactionQuantityDto;
import tech.onlycoders.backend.dto.tag.response.ReadDisplayedTagDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.model.PostType;
import tech.onlycoders.backend.model.ReactionType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadPostDto {

  private String Id;
  private ReadUserLiteDto publisher;
  private String message;
  private PostType type;
  private Boolean isPublic;
  private String url;
  private List<ReadUserLiteDto> mentions;
  private List<ReadDisplayedTagDto> tags;
  private List<ReactionQuantityDto> reactions;
  private Long commentQuantity;
  private ReactionType myReaction;
  private Date createdAt;
  private Boolean isFavorite;
}
