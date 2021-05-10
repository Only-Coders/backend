package tech.onlycoders.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserDto {

  private String firstName;
  private String lastName;
  private String email;
  private String imageURI;
  private String canonicalName;
  private Boolean defaultPrivacyIsPublic;
}
