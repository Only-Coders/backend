package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.mapper.CountryMapper;
import tech.onlycoders.backend.repository.CountryRepository;

@RunWith(MockitoJUnitRunner.class)
public class CountryServiceTest {

  @InjectMocks
  private CountryService service;

  @Mock
  private CountryRepository countryRepository;

  @Before
  public void setUp() {
    var countryMapper = Mappers.getMapper(CountryMapper.class);
    ReflectionTestUtils.setField(service, "countryMapper", countryMapper);
  }

  @Test
  public void ShouldFailWhenFirebaseReturnsException() {
    Mockito.when(this.countryRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(new ArrayList<>());
    var countries = this.service.findCountries("Uruguay");
    assertNotNull(countries);
  }
}
