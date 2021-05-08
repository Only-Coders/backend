package tech.onlycoders.backend.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.DateLong;

@Node
@Data
@NoArgsConstructor
public class Tag {

  @Id
  private String canonicalName;

  private String name;

  @LastModifiedDate
  @DateLong
  private Date updatedAt;

  @DateLong
  @CreatedDate
  private Date createdAt;

  @Relationship(type = "HAS", direction = Relationship.Direction.INCOMING)
  public Set<Post> posts = new HashSet<>();
}
