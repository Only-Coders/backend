package tech.onlycoders.backend.dto.report.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostReportDto {

  private String reason;

  @NotNull(message = "type is required.")
  @NotBlank(message = "Reason is required.")
  private String typeID;
}
