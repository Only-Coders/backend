package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.DateLong;

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
