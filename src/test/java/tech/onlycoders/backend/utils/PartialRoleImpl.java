package tech.onlycoders.backend.utils;

import tech.onlycoders.backend.repository.projections.PartialUser;

public class PartialRoleImpl implements PartialUser.PartialRole {

  private String name;

  public PartialRoleImpl() {}

  public PartialRoleImpl(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return this.name;
  }
}
