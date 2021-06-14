package tech.onlycoders.backend.dto.user.request;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddFCMTokenDto {

  @NotEmpty(message = "FCM Token is required")
  private String fcmToken;
}
