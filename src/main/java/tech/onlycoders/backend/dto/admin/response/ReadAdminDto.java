package tech.onlycoders.backend.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadAdminDto {

  private String firstName;
  private String lastName;
  private String email;
  private String imageURI;
}
