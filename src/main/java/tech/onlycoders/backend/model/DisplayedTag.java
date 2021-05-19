package tech.onlycoders.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisplayedTag {

  @Id
  @GeneratedValue
  private Long id;

  @TargetNode
  private Tag tag;

  private String displayName;
}
