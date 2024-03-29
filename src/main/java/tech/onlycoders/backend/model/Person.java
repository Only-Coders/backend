package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.DateLong;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person extends BaseEntity {

  private String firstName;
  private String lastName;
  private String fullName; // Calculated field
  private String email;
  private String imageURI;
  private String canonicalName;
  private Boolean blocked = false;

  @DateLong
  private Date securityUpdate;

  @Relationship(type = "HAS", direction = Relationship.Direction.OUTGOING)
  public Role role;
}
