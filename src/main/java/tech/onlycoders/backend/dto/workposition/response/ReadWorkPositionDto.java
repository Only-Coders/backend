package tech.onlycoders.backend.dto.workposition.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.DateLong;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;
import tech.onlycoders.backend.model.Workplace;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadWorkPositionDto {

  private ReadWorkplaceDto workplace;

  private Date since;

  private Date until;

  private String position;
}
