package tech.onlycoders.backend.dto;

public enum SortUsersBy {
  FIRSTNAME("firstName"),
  LASTNAME("lastName"),
  MEDALS("medals");

  public final String label;

  SortUsersBy(String label) {
    this.label = label;
  }
}
