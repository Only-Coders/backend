package tech.onlycoders.backend.dto.notificationConfiguration.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.notificator.dto.EventType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationConfigDto {

  @NotNull(message = "type is required.")
  @NotBlank(message = "type is required.")
  private EventType type;

  @NotNull(message = "email is required.")
  @NotBlank(message = "email is required.")
  private Boolean email;

  @NotNull(message = "push is required.")
  @NotBlank(message = "push is required.")
  private Boolean push;
}
