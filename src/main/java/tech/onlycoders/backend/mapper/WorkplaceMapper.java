package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;
import tech.onlycoders.backend.model.Workplace;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface WorkplaceMapper {
  List<ReadWorkplaceDto> listWorkplacesToListReadWorkplaceDto(List<Workplace> workplaces);
  ReadWorkplaceDto workplaceToReadWorkplaceDto(Workplace workplace);
}
