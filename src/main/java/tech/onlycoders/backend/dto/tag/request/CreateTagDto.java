package tech.onlycoders.backend.dto.tag.request;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.support.DateLong;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTagDto {

  private String name;
}
