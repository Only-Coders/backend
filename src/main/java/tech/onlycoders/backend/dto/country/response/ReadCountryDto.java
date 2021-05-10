package tech.onlycoders.backend.dto.country.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ReadCountryDto {

  private String name;
  private String code;
}
