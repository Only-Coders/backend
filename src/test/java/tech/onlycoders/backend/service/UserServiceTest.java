package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
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
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
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
  private AuthService authService;

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

  @Mock
  private SkillRepository skillRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Mock
  private TagRepository tagRepository;

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
    Mockito.when(this.authService.postCreateUser(any(User.class))).thenReturn(new AuthResponseDto());
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
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.organizationRepository.findById(workExperienceDto.getId())).thenReturn(Optional.of(organization));

    this.service.addWork(email, workExperienceDto);
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenOrganizationNotFound() {
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.organizationRepository.findById(workExperienceDto.getId())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWork(email, workExperienceDto));
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenUserNotFound() {
    var organization = ezRandom.nextObject(Organization.class);
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.organizationRepository.findById(workExperienceDto.getId())).thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWork(email, workExperienceDto));
  }

  @Test
  public void ShouldAddSchool() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var organization = ezRandom.nextObject(EducationalOrganization.class);
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito
      .when(this.educationalOrganizationRepository.findById(educationExperienceDto.getId()))
      .thenReturn(Optional.of(organization));

    this.service.addSchool(email, educationExperienceDto);
  }

  @Test
  public void ShouldFailToAddSchoolWhenOrganizationNotFound() {
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito
      .when(this.educationalOrganizationRepository.findById(educationExperienceDto.getId()))
      .thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addSchool(email, educationExperienceDto));
  }

  @Test
  public void ShouldFailToAddSchoolWhenUserNotFound() {
    var organization = ezRandom.nextObject(EducationalOrganization.class);
    var educationExperienceDto = ezRandom.nextObject(EducationExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito
      .when(this.educationalOrganizationRepository.findById(educationExperienceDto.getId()))
      .thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addSchool(email, educationExperienceDto));
  }

  @Test
  public void ShouldAddSkill() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var skill = ezRandom.nextObject(Skill.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.skillRepository.findById(skill.getCanonicalName())).thenReturn(Optional.of(skill));

    this.service.addSkill(email, skill.getCanonicalName());
  }

  @Test
  public void ShouldFailToAddSchoolWhenSkillNotFound() {
    var skill = ezRandom.nextObject(Skill.class);
    var email = ezRandom.nextObject(String.class);
    assertThrows(ApiException.class, () -> this.service.addSkill(email, skill.getCanonicalName()));
  }

  @Test
  public void ShouldAddTag() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    this.service.addTag(email, tag.getCanonicalName());
  }

  @Test
  public void ShouldFailToAddTagWhenTagNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    assertThrows(ApiException.class, () -> this.service.addTag(email, tag.getCanonicalName()));
  }

  @Test
  public void ShouldFailToAddTagWhenUserNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    assertThrows(ApiException.class, () -> this.service.addTag(email, tag.getCanonicalName()));
  }

  @Test
  public void ShouldFollowUser() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.of(user2));

    this.service.followUser(email, cName);
  }

  @Test
  public void ShouldFailFollowUserWhenWrongEmail() throws ApiException {
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.followUser(email, cName));
  }

  @Test
  public void ShouldFailFollowUserWhenWrongCanonicalName() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.followUser(email, cName));
  }

  @Test
  public void ShouldReturnSuggestedUsers() throws ApiException {
    var email = ezRandom.nextObject(String.class);
    var list = new ArrayList<User>();
    list.add(ezRandom.nextObject(User.class));
    Mockito.when(this.userRepository.findSuggestedUsers(anyString(), anyInt())).thenReturn(list);
    var listdto = this.service.getSuggestedUsers(email, 1);
    assertNotNull(listdto);
  }

  @Test
  public void ShouldSendRequest() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.of(user2));

    this.service.sendContactRequest(email, reqDto);
  }

  @Test
  public void ShouldFailSendRequestUserWhenWrongEmail() throws ApiException {
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(email, reqDto));
  }

  @Test
  public void ShouldFailSendRequestUserWhenCanonicalName() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(email, reqDto));
  }
}
