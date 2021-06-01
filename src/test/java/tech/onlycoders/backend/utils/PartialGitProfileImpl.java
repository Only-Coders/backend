package tech.onlycoders.backend.utils;

import tech.onlycoders.backend.dto.user.GitPlatform;
import tech.onlycoders.backend.repository.projections.PartialUser;

public class PartialGitProfileImpl implements PartialUser.PartialGitProfile {

  private String username;

  public PartialGitProfileImpl() {}

  public PartialGitProfileImpl(String username) {
    this.username = username;
  }

  @Override
  public PartialGitPlatform getPlatform() {
    return GitPlatform.GITHUB::toString;
  }

  @Override
  public String getUsername() {
    return username;
  }
}
