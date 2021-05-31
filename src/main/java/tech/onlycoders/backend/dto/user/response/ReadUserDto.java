package tech.onlycoders.backend.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;

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
  private GitProfileDto gitProfile;
  private Integer medalQty;
  private Integer followerQty;
  private Integer contactQty;
  private Integer postQty;
  private ReadWorkPositionDto currentPosition;
  private boolean isConnected;
  private boolean isFollowing;
  private boolean pendingRequest;
}
