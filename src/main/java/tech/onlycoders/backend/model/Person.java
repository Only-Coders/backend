package tech.onlycoders.backend.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.DateLong;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class Person extends BaseEntity {

  private String firstName;
  private String lastName;
  private String email;
  private String img;
  private String imageURI;
  private String canonicalName;
  private Boolean defaultPrivacyIsPublic = false;

  @DateLong
  private Date deleteAt;

  @DateLong
  private Date securityUpdate;

  private Boolean blocked;

  @Relationship("WORKS_AT")
  private Set<WorksAt> workingPlaces = new HashSet<>();

  @Relationship("STUDIES_AT")
  private Set<StudiesAt> schools = new HashSet<>();

  @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
  public Set<Person> followed = new HashSet<>();

  @Relationship(type = "IS_CONNECTED", direction = Relationship.Direction.OUTGOING)
  public Set<Person> contacts = new HashSet<>();

  @Relationship(type = "PUBLISH", direction = Relationship.Direction.OUTGOING)
  public Set<Post> posts = new HashSet<>();

  @Relationship(type = "IS_INTERESTED", direction = Relationship.Direction.OUTGOING)
  public Set<Tag> tags = new HashSet<>();

  @Relationship(type = "CONFIGURES", direction = Relationship.Direction.OUTGOING)
  public Set<NotificationConfig> configs = new HashSet<>();

  @Relationship(type = "SENDS", direction = Relationship.Direction.OUTGOING)
  public Set<ContactRequest> requests = new HashSet<>();

  @Relationship(type = "LIVES", direction = Relationship.Direction.OUTGOING)
  public Country country;

  @Relationship(type = "POSSESS", direction = Relationship.Direction.OUTGOING)
  public Set<Skill> skills = new HashSet<>();

  @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
  public Role role;
}
