package tech.onlycoders.backend.dto.blacklist.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadBlackListDto {

  @Id
  private String email;

  private Date createdAt;
}
