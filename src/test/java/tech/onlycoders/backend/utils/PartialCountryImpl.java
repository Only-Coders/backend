package tech.onlycoders.backend.utils;

import tech.onlycoders.backend.repository.projections.PartialUser;

public class PartialCountryImpl implements PartialUser.PartialCountry {

  private String name;
  private String code;

  public PartialCountryImpl(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getCode() {
    return null;
  }
}
