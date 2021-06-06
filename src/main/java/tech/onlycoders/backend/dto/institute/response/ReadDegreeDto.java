package tech.onlycoders.backend.dto.institute.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadDegreeDto {

  private String id;

  private ReadInstituteDto institute;

  private Date since;

  private Date until;

  private String degree;
}
