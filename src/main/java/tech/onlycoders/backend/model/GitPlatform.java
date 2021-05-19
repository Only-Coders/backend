package tech.onlycoders.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitPlatform {

  @Id
  private String id;

  private String name;
  //  @Version
  //  private Long version = 0L;
}
