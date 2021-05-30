package tech.onlycoders.backend.dto.report.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.onlycoders.backend.model.ReportType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostReportDto {

  @NotNull(message = "Reason is required.")
  @NotBlank(message = "Reason is required.")
  private String reason;

  @NotNull(message = "type is required.")
  @NotBlank(message = "Reason is required.")
  private String typeID;
}
