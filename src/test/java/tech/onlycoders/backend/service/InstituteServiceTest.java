package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.*;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.institute.request.UpdateDegreeDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.workposition.request.UpdateWorkPositionDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.InstituteMapper;
import tech.onlycoders.backend.model.Degree;
import tech.onlycoders.backend.model.Institute;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.repository.DegreeRepository;
import tech.onlycoders.backend.repository.InstituteRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.PartialUserImpl;

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
  private final InstituteMapper instituteMapper = Mappers.getMapper(InstituteMapper.class);

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
    var user = ezRandom.nextObject(PartialUserImpl.class);
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

  @MockitoSettings(strictness = Strictness.LENIENT)
  @Test
  public void ShouldGetWorkingExperiencesUser() {
    var canonicalName = "canonical";
    var page = ezRandom.nextInt();
    var size = ezRandom.nextInt();
    var workPositions = ezRandom.objects(Degree.class, 10).collect(Collectors.toList());

    Mockito.when(this.degreeRepository.getUserDegrees(canonicalName, page, size)).thenReturn(workPositions);
    Mockito.when(this.degreeRepository.countUserDegrees(canonicalName)).thenReturn(3);
    var result = this.service.getUserDegrees(canonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  public void ShouldRemoveWorkExperience() throws ApiException {
    Mockito.when(this.degreeRepository.isOwner(anyString(), anyString())).thenReturn(true);

    this.service.removeDegree("email", "id");
  }

  @Test
  public void ShouldFailRemoveWorkExperienceWhenIsNotOwner() throws ApiException {
    Mockito.when(this.degreeRepository.isOwner(anyString(), anyString())).thenReturn(false);

    assertThrows(Exception.class, () -> this.service.removeDegree("email", "id"));
  }

  @Test
  public void ShouldUpdateDegree() throws ApiException {
    var institute = ezRandom.nextObject(Institute.class);
    var degree = ezRandom.nextObject(Degree.class);
    var updateDto = ezRandom.nextObject(EducationExperienceDto.class);
    var degreeId = ezRandom.nextObject(String.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.degreeRepository.findUserDegree(email, degreeId)).thenReturn(Optional.of(degree));
    Mockito.when(this.instituteRepository.findById(updateDto.getId())).thenReturn(Optional.of(institute));

    this.service.updateDegree(email, degreeId, updateDto);
    assertEquals(degree.getDegree(), updateDto.getDegree());
    assertEquals(degree.getSince(), updateDto.getSince());
    assertEquals(degree.getUntil(), updateDto.getUntil());
  }

  @Test
  public void ShouldFailToUpdateDegree() {
    var updateDto = ezRandom.nextObject(EducationExperienceDto.class);
    var degreeId = ezRandom.nextObject(String.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.degreeRepository.findUserDegree(email, degreeId)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.updateDegree(email, degreeId, updateDto));
  }
}
