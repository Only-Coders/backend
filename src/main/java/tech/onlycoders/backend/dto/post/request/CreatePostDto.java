package tech.onlycoders.backend.dto.post.request;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.model.PostType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostDto {

  @NotNull(message = "message is mandatory")
  private String message;

  @NotNull(message = "type is mandatory")
  private PostType type;

  @NotNull(message = "isPublic is mandatory")
  private Boolean isPublic;

  private String url;
  private List<String> mentionCanonicalNames;
  private List<String> tagNames;
}
