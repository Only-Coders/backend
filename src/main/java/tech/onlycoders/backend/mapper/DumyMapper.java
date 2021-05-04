package tech.onlycoders.backend.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.auth.response.TokenDto;
import tech.onlycoders.backend.model.Dummy;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface DumyMapper {
  TokenDto toPersonDto(Dummy person);
}
