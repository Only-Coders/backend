package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.model.Country;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CountryMapper {
  List<ReadCountryDto> listCountriesToListReadCountryDto(List<Country> countries);
}
