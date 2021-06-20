package tech.onlycoders.backend.dto.recentsearch.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadRecentSearchDto implements Comparable<ReadRecentSearchDto> {

  private String canonicalName;
  private String fullName;
  private String imageURI;
  private Long createdAt;

  @Override
  public int compareTo(ReadRecentSearchDto o) {
    return createdAt > o.getCreatedAt() ? -1 : 1;
  }
}
