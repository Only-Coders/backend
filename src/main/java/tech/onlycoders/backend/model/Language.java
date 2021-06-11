package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode
@Node
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

  @Id
  private String code;

  private String name;
}
