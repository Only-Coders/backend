package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.DateLong;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPosition extends BaseEntity {

  @Relationship(type = "ON", direction = Relationship.Direction.OUTGOING)
  private Workplace workplace;

  @DateLong
  private Date since;

  @DateLong
  private Date until;

  private String position;
}
