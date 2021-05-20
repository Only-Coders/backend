package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.DateLong;

@Node
@Data
@NoArgsConstructor
public class BlackList {

  @Id
  private String email;

  @DateLong
  @CreatedDate
  private Date createdAt;
  //  @Version
  //  private Long version;
}
