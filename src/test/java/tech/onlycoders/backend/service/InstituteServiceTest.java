package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.InstituteMapper;
import tech.onlycoders.backend.model.Institute;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.DegreeRepository;
import tech.onlycoders.backend.repository.InstituteRepository;
import tech.onlycoders.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class InstituteServiceTest {

  @InjectMocks
  private InstituteService service;

  @Mock
  private InstituteRepository instituteRepository;

  @Mock
  private DegreeRepository degreeRepository;

  @Mock
  private UserRepository userRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final InstituteMapper countryMapper = Mappers.getMapper(InstituteMapper.class);

  @Test
  public void ShouldPaginateOrganizations() {
    var organizations = ezRandom.objects(Institute.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(organizations);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.instituteRepository.findByNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listInstitutes("asd", 1, 1);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewOrganization() {
    var createOrganizationDto = ezRandom.nextObject(CreateInstituteDto.class);
    var org = new Institute();
    org.setName(createOrganizationDto.getName());
    Mockito.when(this.instituteRepository.save(any(Institute.class))).thenReturn(org);
    var result = this.service.createInstitute(createOrganizationDto);
    assertEquals(createOrganizationDto.getName(), result.getName());
  }

  @Test
  public void ShouldAddSchool() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var organization = ezRandom.nextObject(Institute.class);
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito
      .when(this.instituteRepository.findById(educationExperienceDto.getId()))
      .thenReturn(Optional.of(organization));

    this.service.addUserDegree(email, educationExperienceDto);
  }

  @Test
  public void ShouldFailToAddSchoolWhenOrganizationNotFound() {
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.instituteRepository.findById(educationExperienceDto.getId())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addUserDegree(email, educationExperienceDto));
  }

  @Test
  public void ShouldFailToAddSchoolWhenUserNotFound() {
    var organization = ezRandom.nextObject(Institute.class);
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito
      .when(this.instituteRepository.findById(educationExperienceDto.getId()))
      .thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addUserDegree(email, educationExperienceDto));
  }
}
