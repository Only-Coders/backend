package tech.onlycoders.backend.dto.notificator;

public enum EventType {
  NEW_POST, //notify followers and friends
  NEW_COMMENT, //notify owner
  NEW_MENTION, //notify target
  CONTACT_REQUEST, //notify target
  CONTACT_ACCEPTED, //notify source
  NEW_FOLLOWER, //notify target
  NEW_ADMIN_ACCOUNT
}
