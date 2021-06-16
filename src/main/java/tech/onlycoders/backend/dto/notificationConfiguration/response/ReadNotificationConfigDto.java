package tech.onlycoders.backend.dto.notificationConfiguration.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.notificator.dto.EventType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadNotificationConfigDto {

  private String id;
  private EventType type;
  private Boolean email;
  private Boolean push;
}
