package tech.onlycoders.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class ContactRequest extends BaseEntity {

  private String message;

  @Relationship(type = "TO", direction = Relationship.Direction.OUTGOING)
  public Person receiver;
}
