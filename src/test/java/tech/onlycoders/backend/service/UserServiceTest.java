package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @InjectMocks
  private UserService service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private OrganizationRepository organizationRepository;

  @Mock
  private EducationalOrganizationRepository educationalOrganizationRepository;

  @Mock
  private GitPlatformRepository gitPlatformRepository;

  @Mock
  private GitProfileRepository gitProfileRepository;

  @Mock
  private CountryRepository countryRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Before
  public void setUp() {
    var userMapper = Mappers.getMapper(UserMapper.class);
    ReflectionTestUtils.setField(service, "userMapper", userMapper);
  }

  @Test
  public void ShouldFailWhenFirebaseReturnsException() {
    var canonicalName = ezRandom.nextObject(String.class);
    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());
    assertThrows(ApiException.class, () -> this.service.getProfile(canonicalName));
  }

  @Test
  public void ShouldReturnUserProfile() throws ApiException {
    var canonicalName = ezRandom.nextObject(String.class);
    Mockito
      .when(this.userRepository.findByCanonicalName(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(User.class)));
    var profile = this.service.getProfile(canonicalName);
    assertNotNull(profile);
  }

  @Test
  public void ShouldCreateNewUser() throws ApiException {
    var createUserDto = ezRandom.nextObject(CreateUserDto.class);
    var country = ezRandom.nextObject(Country.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Mockito.when(this.countryRepository.findById(anyString())).thenReturn(Optional.of(country));
    Mockito
      .when(this.gitPlatformRepository.findById(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(GitPlatform.class)));
    this.service.createUser(email, createUserDto);
  }

  @Test
  public void ShouldFailToCreateNewUser() {
    var createUserDto = ezRandom.nextObject(CreateUserDto.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    assertThrows(ApiException.class, () -> this.service.createUser(email, createUserDto));
  }

  @Test
  public void ShouldFailToCreateNewUserWhenGitPlatformDoesNotExist() {
    var createUserDto = ezRandom.nextObject(CreateUserDto.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    assertThrows(ApiException.class, () -> this.service.createUser(email, createUserDto));
  }

  @Test
  public void ShouldFailToCreateNewUserWhenCountryNotFound() {
    var createUserDto = ezRandom.nextObject(CreateUserDto.class);
    var email = ezRandom.nextObject(String.class);

    assertThrows(ApiException.class, () -> this.service.createUser(email, createUserDto));
  }

  @Test
  public void ShouldAddWorkingExperience() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var organization = ezRandom.nextObject(Organization.class);
    var createUserDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

    this.service.addWork(email, organizationId, createUserDto);
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenOrganizationNotFound() {
    var createUserDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.organizationRepository.findById(organizationId)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWork(email, organizationId, createUserDto));
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenUserNotFound() {
    var organization = ezRandom.nextObject(Organization.class);
    var createUserDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWork(email, organizationId, createUserDto));
  }

  @Test
  public void ShouldAddSchool() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var organization = ezRandom.nextObject(EducationalOrganization.class);
    var createUserDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.educationalOrganizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));

    this.service.addSchool(email, organizationId, createUserDto);
  }

  @Test
  public void ShouldFailToAddSchoolWhenOrganizationNotFound() {
    var createUserDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.educationalOrganizationRepository.findById(organizationId)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addSchool(email, organizationId, createUserDto));
  }

  @Test
  public void ShouldFailToAddSchoolWhenUserNotFound() {
    var organization = ezRandom.nextObject(EducationalOrganization.class);
    var createUserDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);
    var organizationId = ezRandom.nextObject(String.class);

    Mockito.when(this.educationalOrganizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addSchool(email, organizationId, createUserDto));
  }
}
