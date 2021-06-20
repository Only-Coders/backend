package tech.onlycoders.backend.dto.user.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadUserDto {

  private String firstName;
  private String lastName;
  private String fullName;
  private String description;
  private String email;
  private String imageURI;
  private String canonicalName;
  private Boolean defaultPrivacyIsPublic;
  private GitProfileDto gitProfile;
  private Integer medalQty;
  private Integer followingQty;
  private Integer followerQty;
  private Integer contactQty;
  private Integer postQty;
  private ReadWorkPositionDto currentPosition;
  private boolean isConnected;
  private boolean isFollowing;
  private boolean pendingRequest;
  private boolean requestHasBeenSent;
  private ReadCountryDto country;
  private Date birthDate;
}
