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
public class Report extends BaseEntity {

  private String reason;

  @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
  public ReportType type;

  @Relationship(type = "FOR", direction = Relationship.Direction.OUTGOING)
  public Post post;

  @Relationship(type = "CREATES", direction = Relationship.Direction.INCOMING)
  public Person reporter;
}
