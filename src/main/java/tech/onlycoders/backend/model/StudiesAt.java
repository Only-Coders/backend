package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.support.DateLong;

@RelationshipProperties
@Data
@NoArgsConstructor
public class StudiesAt {

  @TargetNode
  private EducationalOrganization organization;

  @DateLong
  private Date since;

  @DateLong
  private Date until;

  private String degree;
}
