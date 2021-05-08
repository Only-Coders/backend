package tech.onlycoders.backend.dto.post.request;

import java.util.List;
import javax.validation.constraints.NotBlank;
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

  @NotBlank(message = "Token is mandatory")
  private String message;

  @NotBlank(message = "Token is mandatory")
  private PostType type;

  @NotBlank(message = "Token is mandatory")
  private Boolean isPublic;

  private String url;
  private List<String> mentionCanonicalNames;
  private List<String> tagNames;
}
