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
public class Post extends BaseEntity{
    private String message;
    private PostType type;
    private Boolean isPublic;

    @Relationship(type = "FOR", direction = Relationship.Direction.INCOMING)
    public Set<Comment> comments;

    @Relationship(type = "TO", direction = Relationship.Direction.INCOMING)
    public Set<Reaction> reactions;

    @Relationship(type = "MENTIONS", direction = Relationship.Direction.OUTGOING)
    public Set<Person> mentions;

    @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
    public Set<Tag> tags;

}
