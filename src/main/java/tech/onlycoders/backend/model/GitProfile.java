package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@EqualsAndHashCode(callSuper = true)
@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitProfile extends BaseEntity {

  @TargetNode
  private GitPlatform platform;

  private String username;
}
