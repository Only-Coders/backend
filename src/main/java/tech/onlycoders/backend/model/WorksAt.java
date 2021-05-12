package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.support.DateLong;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@RelationshipProperties
@Data
@NoArgsConstructor
public class WorksAt {

  @Id
  @GeneratedValue(UUIDStringGenerator.class)
  private String id;

  @TargetNode
  private Organization organization;

  @DateLong
  private Date since;

  @DateLong
  private Date until;

  private String position;
}
