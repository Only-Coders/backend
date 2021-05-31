package tech.onlycoders.backend.utils;

import tech.onlycoders.backend.model.PostType;
import tech.onlycoders.backend.repository.projections.PartialPost;

public class PartialPostImpl implements PartialPost {

  private String id;
  private String message;
  private String url;
  private PostType type;
  private Boolean isPublic;

  public PartialPostImpl(String id, String message, String url, PostType type, Boolean isPublic) {
    this.id = id;
    this.message = message;
    this.url = url;
    this.type = type;
    this.isPublic = isPublic;
  }

  public PartialPostImpl() {}

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

  @Override
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public PostType getType() {
    return type;
  }

  @Override
  public Boolean getIsPublic() {
    return isPublic;
  }

  public void setType(PostType type) {
    this.type = type;
  }

  public Boolean getPublic() {
    return isPublic;
  }

  public void setPublic(Boolean aPublic) {
    isPublic = aPublic;
  }
}
