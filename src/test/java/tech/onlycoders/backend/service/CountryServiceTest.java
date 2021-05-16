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
import tech.onlycoders.backend.repository.CountryRepository;

@ExtendWith(MockitoExtension.class)
public class CountryServiceTest {

  @InjectMocks
  private CountryService service;

  @Mock
  private CountryRepository countryRepository;

  @Spy
  private final CountryMapper countryMapper = Mappers.getMapper(CountryMapper.class);

  @Test
  public void ShouldFailWhenFirebaseReturnsException() {
    Mockito.when(this.countryRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(new ArrayList<>());
    var countries = this.service.findCountries("Uruguay");
    assertNotNull(countries);
  }
}
