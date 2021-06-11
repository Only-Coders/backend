package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.dto.language.response.ReadLanguageDto;
import tech.onlycoders.backend.model.Country;
import tech.onlycoders.backend.model.Language;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LanguageMapper {
  List<ReadLanguageDto> listLanguagesToListReadLanguageDto(List<Language> languages);

  ReadLanguageDto LanguageToReadLanguageDto(Language userLanguage);
}
