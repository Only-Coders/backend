package tech.onlycoders.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.dto.language.response.ReadLanguageDto;
import tech.onlycoders.backend.mapper.CountryMapper;
import tech.onlycoders.backend.mapper.LanguageMapper;
import tech.onlycoders.backend.repository.CountryRepository;
import tech.onlycoders.backend.repository.LanguageRepository;

@Service
@Transactional
public class LanguageService {

  private final LanguageRepository repo;
  private final LanguageMapper mapper;

  public LanguageService(LanguageRepository repo, LanguageMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  public List<ReadLanguageDto> getLanguages() {
    return mapper.listLanguagesToListReadLanguageDto(repo.findAll());
  }
}
