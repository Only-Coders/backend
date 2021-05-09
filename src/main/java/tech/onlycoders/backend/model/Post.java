package tech.onlycoders.backend.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class Post extends BaseEntity {

  private String message;
  private PostType type;
  private Boolean isPublic;
  private String url;

  @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
  public Set<Comment> comments = new HashSet<>();

  @Relationship(type = "TO", direction = Relationship.Direction.INCOMING)
  public Set<Reaction> reactions = new HashSet<>();

  @Relationship(type = "MENTIONS", direction = Relationship.Direction.OUTGOING)
  public Set<Person> mentions = new HashSet<>();

  @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
  public Set<Tag> tags = new HashSet<>();

  @Relationship(type = "PUBLISH", direction = Relationship.Direction.INCOMING)
  public Person publisher;
}
