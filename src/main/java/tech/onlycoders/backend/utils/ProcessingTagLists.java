package tech.onlycoders.backend.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.tag.response.ReadTagNameDto;
import tech.onlycoders.backend.model.Tag;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingTagLists {

  private Set<Tag> persitedTags = new HashSet<>();
  private List<ReadTagNameDto> tagNames = new ArrayList<>();
}
