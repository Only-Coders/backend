package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.mapper.CountryMapper;
import tech.onlycoders.backend.mapper.LanguageMapper;
import tech.onlycoders.backend.repository.CountryRepository;
import tech.onlycoders.backend.repository.LanguageRepository;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceTest {

  @InjectMocks
  private LanguageService service;

  @Mock
  private LanguageRepository languageRepository;

  @Spy
  private final LanguageMapper languageMapper = Mappers.getMapper(LanguageMapper.class);

  @Test
  public void ShouldReturnCountries() {
    Mockito.when(this.languageRepository.findAll()).thenReturn(new ArrayList<>());
    var lang = this.service.getLanguages();
    assertNotNull(lang);
  }
}
