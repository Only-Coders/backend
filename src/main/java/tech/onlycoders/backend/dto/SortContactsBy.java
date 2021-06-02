package tech.onlycoders.backend.dto;

public enum SortContactsBy {
  FULLNAME("fullName"),
  MEDALS("medals");

  public final String label;

  SortContactsBy(String label) {
    this.label = label;
  }
}
