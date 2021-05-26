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
public class Comment extends BaseEntity {

  private String message;

  @Relationship(type = "WRITES", direction = Relationship.Direction.INCOMING)
  public Person person;

  @Relationship(type = "TO", direction = Relationship.Direction.INCOMING)
  public Set<Reaction> reactions = new HashSet<>();
}
