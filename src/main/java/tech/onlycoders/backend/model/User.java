package tech.onlycoders.backend.model;

import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Builder
@EqualsAndHashCode(callSuper = true)
@Node({ "User", "Person" })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends Person {

  private Boolean defaultPrivacyIsPublic = false;

  private Boolean blocked;

  @Relationship("WORKS_AT")
  private Set<WorksAt> workingPlaces = new HashSet<>();

  @Relationship("STUDIES_AT")
  private Set<StudiesAt> schools = new HashSet<>();

  @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
  public Set<User> followed = new HashSet<>();

  @Relationship(type = "IS_CONNECTED", direction = Relationship.Direction.OUTGOING)
  public Set<User> contacts = new HashSet<>();

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
}
