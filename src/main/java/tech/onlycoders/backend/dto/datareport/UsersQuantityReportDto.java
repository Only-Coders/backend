package tech.onlycoders.backend.dto.datareport;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.dto.institute.response.ReadInstituteDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersQuantityReportDto {

  private Integer totalActiveUsers;

  private Integer totalBlockedUsers;

  private Integer totalBannedUsers;
}
