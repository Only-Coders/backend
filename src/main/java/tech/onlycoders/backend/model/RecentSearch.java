package tech.onlycoders.backend.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecentSearch implements Serializable, Comparable<RecentSearch> {

  private String canonicalName;
  private String fullName;
  private String imageURI;
  private Long createdAt;

  @Override
  public int compareTo(RecentSearch other) {
    return other.getCreatedAt() > this.createdAt ? 1 : 0;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RecentSearch that = (RecentSearch) o;

    return canonicalName.equals(that.canonicalName);
  }

  @Override
  public int hashCode() {
    return canonicalName.hashCode();
  }
}
