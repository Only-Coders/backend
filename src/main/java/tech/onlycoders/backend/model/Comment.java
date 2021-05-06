package tech.onlycoders.backend.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class Comment extends BaseEntity{
    private String message;

    @Relationship(type = "WRITES", direction = Relationship.Direction.INCOMING)
    public Person person;

    @Relationship(type = "TO", direction = Relationship.Direction.INCOMING)
    public Set<Reaction> reactions;
}
