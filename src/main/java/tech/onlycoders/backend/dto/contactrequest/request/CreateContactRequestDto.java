package tech.onlycoders.backend.dto.contactrequest.request;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateContactRequestDto {

  @NotEmpty
  private String canonicalName;

  private String message;
}
