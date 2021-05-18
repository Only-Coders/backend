package tech.onlycoders.backend.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.support.DateLong;

@RelationshipProperties
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitProfile {

  @Id
  @GeneratedValue
  private Long id;

  @TargetNode
  private GitPlatform platform;

  private String username;

  @LastModifiedDate
  @DateLong
  private Date updatedAt;

  @DateLong
  @CreatedDate
  private Date createdAt;

  @Version
  private Long version;
}
