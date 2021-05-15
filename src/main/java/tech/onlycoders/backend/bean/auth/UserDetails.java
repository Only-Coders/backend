package tech.onlycoders.backend.bean.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetails {

  private String canonicalName;
  private String firstName;
  private String lastName;
  private String roles;
  private String email;
  private String id;
}
