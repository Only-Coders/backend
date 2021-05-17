package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.DateLong;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Degree extends BaseEntity {

  @Relationship(type = "AT", direction = Relationship.Direction.OUTGOING)
  private Institute institute;

  @DateLong
  private Date since;

  @DateLong
  private Date until;

  private String degree;
}
