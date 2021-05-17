package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node({ "Workplace" })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workplace extends BaseEntity {

  private String name;
}
