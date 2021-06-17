package tech.onlycoders.backend.dto.notificationConfiguration.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNotificationConfigDto {

  private Boolean email;

  private Boolean push;
}
