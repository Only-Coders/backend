package tech.onlycoders.backend.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadGenericUserDto {

  public String canonicalName;
  public String firstName;
  public String lastName;
  public String imageURI;
  public ReadRoleDto role;
}
