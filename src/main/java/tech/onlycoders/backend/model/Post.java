package tech.onlycoders.backend.model;

import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
  public Set<User> mentions = new HashSet<>();

  @Relationship(type = "HAS")
  public Set<DisplayedTag> tags = new HashSet<>();

  @Relationship(type = "PUBLISH", direction = Relationship.Direction.INCOMING)
  public User publisher;

  @Relationship(value = "IS_FAVORITE", direction = Relationship.Direction.INCOMING)
  public Set<User> userFavorites = new HashSet<>();
}
