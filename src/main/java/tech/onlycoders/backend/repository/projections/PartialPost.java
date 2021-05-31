package tech.onlycoders.backend.repository.projections;

import tech.onlycoders.backend.model.PostType;

public interface PartialPost {
  String getId();
  String getMessage();
  PostType getType();
  Boolean getIsPublic();
  String getUrl();
  //  PartialUser getPublisher();
}
