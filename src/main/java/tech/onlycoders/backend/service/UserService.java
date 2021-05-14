package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.GitProfile;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.model.StudiesAt;
import tech.onlycoders.backend.model.WorksAt;
import tech.onlycoders.backend.repository.*;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PersonRepository personRepository;
  private final GitPlatformRepository gitPlatformRepository;
  private final GitProfileRepository gitProfileRepository;
  private final OrganizationRepository organizationRepository;
  private final EducationalOrganizationRepository educationalOrganizationRepository;
  private final CountryRepository countryRepository;
  private final SkillRepository skillRepository;

  private final AuthService authService;

  private final UserMapper userMapper;
  private final TagRepository tagRepository;

  public UserService(
    UserRepository userRepository,
    PersonRepository personRepository,
    UserMapper userMapper,
    GitPlatformRepository gitPlatformRepository,
    GitProfileRepository gitProfileRepository,
    OrganizationRepository organizationRepository,
    EducationalOrganizationRepository educationalOrganizationRepository,
    CountryRepository countryRepository,
    SkillRepository skillRepository,
    AuthService authService,
    TagRepository tagRepository
  ) {
    this.userRepository = userRepository;
    this.personRepository = personRepository;
    this.userMapper = userMapper;
    this.gitPlatformRepository = gitPlatformRepository;
    this.gitProfileRepository = gitProfileRepository;
    this.organizationRepository = organizationRepository;
    this.educationalOrganizationRepository = educationalOrganizationRepository;
    this.countryRepository = countryRepository;
    this.skillRepository = skillRepository;
    this.authService = authService;
    this.tagRepository = tagRepository;
  }

  public ReadUserDto getProfile(String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
    return userMapper.userToReadPersonDto(user);
  }

  public AuthResponseDto createUser(String email, CreateUserDto createUserDto) throws ApiException {
    var optionalPerson = this.personRepository.findByEmail(email);
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already taken");
    } else {
      var user = userMapper.createUserDtoToUser(createUserDto);
      user.country =
        countryRepository
          .findById(createUserDto.getCountry().getCode())
          .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Country not found"));
      user.setEmail(email);
      if (createUserDto.getGitProfile() != null) {
        var gitPlatform = gitPlatformRepository
          .findById(createUserDto.getGitProfile().getPlatform().toString())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Git platform not found"));
        var gitUser = GitProfile
          .builder()
          .username(createUserDto.getGitProfile().getUserName())
          .platform(gitPlatform)
          .build();
        user.setGitProfile(gitUser);
        gitProfileRepository.save(gitUser);
      }
      user.setRole(Role.builder().name("USER").build());
      userRepository.save(user);
      return this.authService.postCreateUser(user);
    }
  }

  public void addWork(String email, WorkExperienceDto workExperienceDto) throws ApiException {
    var organization =
      this.organizationRepository.findById(workExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    var worksAt = new WorksAt();
    worksAt.setOrganization(organization);
    worksAt.setSince(workExperienceDto.getSince());
    worksAt.setUntil(workExperienceDto.getUntil());
    user.getWorkingPlaces().add(worksAt);
    this.userRepository.save(user);
  }

  public void addSchool(String email, EducationExperienceDto educationExperienceDto) throws ApiException {
    var organization =
      this.educationalOrganizationRepository.findById(educationExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    var studiesAt = new StudiesAt();
    studiesAt.setOrganization(organization);
    studiesAt.setSince(educationExperienceDto.getSince());
    studiesAt.setUntil(educationExperienceDto.getUntil());
    studiesAt.setDegree(educationExperienceDto.getDegree());
    user.getSchools().add(studiesAt);
    this.userRepository.save(user);
  }

  public void addSkill(String email, String canonicalName) throws ApiException {
    var skill =
      this.skillRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Skill not found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    user.getSkills().add(skill);
    this.userRepository.save(user);
  }

  public void addTag(String email, String canonicalName) throws ApiException {
    var tag =
      this.tagRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tag not found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    user.getTags().add(tag);
    this.userRepository.save(user);
  }
}
