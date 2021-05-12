package tech.onlycoders.backend.dto.skill.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadSkillDto {

  private String name;
  private String canonicalName;
}
