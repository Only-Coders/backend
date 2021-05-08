package tech.onlycoders.backend.dto.tag.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadTagDto {

  public String canonicalName;
  public String name;
  public List<ReadPostDto> posts;
}
