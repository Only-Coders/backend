package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.DateLong;

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

  @Id
  private String name;

  @DateLong
  @CreatedDate
  private Date createdAt;
  //  @Version
  //  private Long version;
}
