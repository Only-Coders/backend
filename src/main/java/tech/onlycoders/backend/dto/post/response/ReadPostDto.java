package tech.onlycoders.backend.dto.post.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.tag.response.ReadDisplayedTagDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.model.PostType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadPostDto {

  private ReadUserLiteDto publisher;
  private String message;
  private PostType type;
  private Boolean isPublic;
  private String url;
  private List<ReadUserLiteDto> mentions;
  private List<ReadDisplayedTagDto> tags;
}
