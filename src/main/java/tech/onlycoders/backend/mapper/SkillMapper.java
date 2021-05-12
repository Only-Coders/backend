package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.skill.response.ReadSkillDto;
import tech.onlycoders.backend.model.Skill;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SkillMapper {
  ReadSkillDto skillToReadSkillDto(Skill skill);
  List<ReadSkillDto> listSkillsToListReadSkillDto(List<Skill> skills);
}
