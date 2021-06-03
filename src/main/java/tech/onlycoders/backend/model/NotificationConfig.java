package tech.onlycoders.backend.model;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import tech.onlycoders.notificator.dto.EventType;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NotificationConfig extends BaseEntity {

  private EventType type;
  private Boolean email;
  private Boolean push;
}
