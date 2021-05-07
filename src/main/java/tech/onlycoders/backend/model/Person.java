package tech.onlycoders.backend.model;

import java.util.Date;
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

  @DateLong
  private Date deleteAt;

  private Boolean blocked;

  @Relationship("WORKS_AT")
  private Set<WorksAt> workingPlaces;

  @Relationship("STUDIES_AT")
  private Set<StudiesAt> schools;

  @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
  public Set<Person> followed;

  @Relationship(type = "IS_CONNECTED", direction = Relationship.Direction.OUTGOING)
  public Set<Person> contacts;

  @Relationship(type = "PUBLISH", direction = Relationship.Direction.OUTGOING)
  public Set<Post> posts;

  @Relationship(type = "IS_INTERESTED", direction = Relationship.Direction.OUTGOING)
  public Set<Tag> tags;

  @Relationship(type = "CONFIGURES", direction = Relationship.Direction.OUTGOING)
  public Set<NotificationConfig> configs;

  @Relationship(type = "SENDS", direction = Relationship.Direction.OUTGOING)
  public Set<ContactRequest> requests;

  @Relationship(type = "LIVES", direction = Relationship.Direction.OUTGOING)
  public Country country;

  @Relationship(type = "POSSESS", direction = Relationship.Direction.OUTGOING)
  public Set<Skill> skills;

  @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
  public Role role;
}
