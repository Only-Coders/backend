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
public class ContactRequest extends BaseEntity {

  private String message;

  @Relationship(type = "TO", direction = Relationship.Direction.OUTGOING)
  public Person target;
}
