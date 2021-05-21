package tech.onlycoders.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.mapper.CountryMapper;
import tech.onlycoders.backend.repository.CountryRepository;

@Service
@Transactional
public class CountryService {

  private final CountryRepository countryRepository;
  private final CountryMapper countryMapper;

  public CountryService(CountryRepository countryRepository, CountryMapper countryMapper) {
    this.countryRepository = countryRepository;
    this.countryMapper = countryMapper;
  }

  public List<ReadCountryDto> findCountries(String countryName) {
    var result = this.countryRepository.findByNameContainingIgnoreCase(countryName);
    return this.countryMapper.listCountriesToListReadCountryDto(result);
  }
}
