package tech.onlycoders.backend.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.model.WorkPosition;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { WorkplaceMapper.class })
public interface WorkPositionMapper {
  ReadWorkPositionDto workPositionToReadWorkPositionDto(WorkPosition workPosition);
}
