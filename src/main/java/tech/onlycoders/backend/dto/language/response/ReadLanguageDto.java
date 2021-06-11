package tech.onlycoders.backend.dto.language.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ReadLanguageDto {

  private String name;
  private String code;
}
