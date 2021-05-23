package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction extends BaseEntity {

  private ReactionType type;

  @Relationship(type = "MAKES", direction = Relationship.Direction.INCOMING)
  public Person person;
}
