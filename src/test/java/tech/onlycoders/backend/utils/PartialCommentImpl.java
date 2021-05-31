package tech.onlycoders.backend.utils;

import tech.onlycoders.backend.repository.projections.PartialComment;

public class PartialCommentImpl implements PartialComment {

  private String id;
  private String message;

  public PartialCommentImpl(String id, String message) {
    this.id = id;
    this.message = message;
  }

  public PartialCommentImpl() {}

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
