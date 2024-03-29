package tech.onlycoders.backend.repository.projections;

import java.util.Date;
import java.util.Optional;

public interface PartialUser {
  Optional<Boolean> getDefaultPrivacyIsPublic();
  Optional<Boolean> getBlocked();
  Optional<Date> getEliminationDate();
  Optional<Date> getBirthDate();
  Optional<PartialLanguage> getLanguage();

  Date getCreatedAt();

  String getFirstName();

  String getDescription();

  String getLastName();

  String getFullName();

  String getEmail();

  String getImageURI();

  String getCanonicalName();

  Date getSecurityUpdate();

  String getId();

  PartialRole getRole();

  PartialCountry getCountry();

  PartialGitProfile getGitProfile();

  interface PartialGitProfile {
    interface PartialGitPlatform {
      String getId();
    }

    PartialGitPlatform getPlatform();

    String getUsername();
  }

  interface PartialRole {
    String getName();
  }

  interface PartialCountry {
    String getName();

    String getCode();
  }

  interface PartialLanguage {
    String getName();

    String getCode();
  }
}
