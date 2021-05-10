package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.person.response.ReadPersonDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PersonMapper;
import tech.onlycoders.backend.repository.PersonRepository;

@Service
public class PersonService {

  private final PersonRepository personRepository;
  private final PersonMapper personMapper;

  public PersonService(PersonRepository personRepository, PersonMapper personMapper) {
    this.personRepository = personRepository;
    this.personMapper = personMapper;
  }

  public ReadPersonDto getProfile(String canonicalName) throws ApiException {
    var person =
      this.personRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
    return personMapper.personToReadPersonDto(person);
  }
}
