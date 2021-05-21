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
public class ContactRequest extends BaseEntity {

  private String message;

  @Relationship(type = "TO", direction = Relationship.Direction.OUTGOING)
  public Person target;

  @Relationship(type = "SENDS", direction = Relationship.Direction.INCOMING)
  public Person requester;
}
