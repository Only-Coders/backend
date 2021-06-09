package tech.onlycoders.backend.utils;

import java.util.Date;
import java.util.Optional;
import tech.onlycoders.backend.repository.projections.PartialUser;

public class PartialUserImpl implements PartialUser {

  private String id;
  private Boolean defaultPrivacyIsPublic;
  private Boolean blocked;
  private Date eliminationDate;

  private String firstName;
  private String lastName;
  private String fullName;
  private String email;
  private String imageURI;
  private String canonicalName;
  private Date securityUpdate;
  private PartialRoleImpl partialRoleImpl;
  private PartialGitProfileImpl gitProfile;
  private PartialCountryImpl country;

  public PartialUserImpl() {}

  public PartialUserImpl(
    String id,
    Boolean defaultPrivacyIsPublic,
    Boolean blocked,
    Date eliminationDate,
    String firstName,
    String lastName,
    String fullName,
    String email,
    String imageURI,
    String canonicalName,
    Date securityUpdate,
    PartialRoleImpl partialRoleImpl,
    PartialGitProfileImpl gitProfile,
    PartialCountryImpl country
  ) {
    this.id = id;
    this.defaultPrivacyIsPublic = defaultPrivacyIsPublic;
    this.blocked = blocked;
    this.eliminationDate = eliminationDate;
    this.firstName = firstName;
    this.lastName = lastName;
    this.fullName = fullName;
    this.email = email;
    this.imageURI = imageURI;
    this.canonicalName = canonicalName;
    this.securityUpdate = securityUpdate;
    this.partialRoleImpl = partialRoleImpl;
    this.gitProfile = gitProfile;
    this.country = country;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setImageURI(String imageURI) {
    this.imageURI = imageURI;
  }

  public void setCanonicalName(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  public void setSecurityUpdate(Date securityUpdate) {
    this.securityUpdate = securityUpdate;
  }

  public void setRole(PartialRoleImpl partialRoleImpl) {}

  public void setGitProfile(PartialGitProfileImpl gitProfile) {
    this.gitProfile = gitProfile;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDefaultPrivacyIsPublic(Boolean defaultPrivacyIsPublic) {
    this.defaultPrivacyIsPublic = defaultPrivacyIsPublic;
  }

  public void setBlocked(Boolean blocked) {
    this.blocked = blocked;
  }

  public void setEliminationDate(Date eliminationDate) {
    this.eliminationDate = eliminationDate;
  }

  public void setPartialRoleImpl(PartialRoleImpl partialRoleImpl) {
    this.partialRoleImpl = partialRoleImpl;
  }

  public void setCountry(PartialCountryImpl country) {
    this.country = country;
  }

  @Override
  public Optional<Boolean> getDefaultPrivacyIsPublic() {
    return Optional.empty();
  }

  @Override
  public Optional<Boolean> getBlocked() {
    return Optional.empty();
  }

  @Override
  public Optional<Date> getEliminationDate() {
    return Optional.empty();
  }

  @Override
  public Optional<Date> getBirthDate() {
    return Optional.of(new Date());
  }

  @Override
  public Date getCreatedAt() {
    return null;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public String getFullName() {
    return fullName;
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public String getImageURI() {
    return imageURI;
  }

  @Override
  public String getCanonicalName() {
    return canonicalName;
  }

  @Override
  public Date getSecurityUpdate() {
    return securityUpdate;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public PartialRoleImpl getRole() {
    return partialRoleImpl;
  }

  @Override
  public PartialCountry getCountry() {
    return country;
  }

  @Override
  public PartialGitProfile getGitProfile() {
    return gitProfile;
  }
}
