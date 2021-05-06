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
public class NotificationConfig extends BaseEntity{
    private NotificationType type;
    private Boolean email;
    private Boolean push;


}
