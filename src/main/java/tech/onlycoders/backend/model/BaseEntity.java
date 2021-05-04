package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.support.DateLong;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Data
public abstract class BaseEntity {

  @Id
  @GeneratedValue(UUIDStringGenerator.class)
  private String id;

  @LastModifiedDate
  @DateLong
  private Date updatedAt;

  @DateLong
  @CreatedDate
  private Date createdAt;
}
