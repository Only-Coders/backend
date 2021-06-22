package tech.onlycoders.backend.dto;

public enum SortAllUsersBy {
  FULLNAME("fullName"),
  BLOCKED("blocked");

  public final String label;

  SortAllUsersBy(String label) {
    this.label = label;
  }
}
