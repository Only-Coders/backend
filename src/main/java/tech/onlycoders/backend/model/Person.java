package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Node;

@EqualsAndHashCode(callSuper = true)
@Node
@Data
@NoArgsConstructor
public class Person extends BaseEntity {

  private String firstName;
  private String lastName;
  private String email;
  private String imageUrl;
  private String c_name;
  private Boolean blocked = false;
  private Date DeleteOn;

  public Person(String firstName, String lastName, String email, String imageUrl) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.imageUrl = imageUrl;
    Integer number = (new Date()).hashCode();
    this.c_name = firstName.toLowerCase() + lastName.toLowerCase() + number.toString();
  }
}
