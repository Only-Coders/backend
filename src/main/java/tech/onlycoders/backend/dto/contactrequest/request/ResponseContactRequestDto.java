package tech.onlycoders.backend.dto.contactrequest.request;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseContactRequestDto {

  @NotEmpty
  private String requesterCanonicalName;

  @NotNull
  private Boolean acceptContact;
}
