package tech.onlycoders.backend.dto.recentsearch.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateRecentSearchDto {

  private String canonicalName;
  private String fullName;
  private String imageURI;
}
