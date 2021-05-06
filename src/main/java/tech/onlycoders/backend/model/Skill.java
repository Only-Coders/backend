package tech.onlycoders.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class Skill extends BaseEntity {
    private String name;
    private String cannonicalName;
}
