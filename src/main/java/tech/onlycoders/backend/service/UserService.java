package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.GitProfile;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.model.WorksAt;
import tech.onlycoders.backend.repository.*;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PersonRepository personRepository;
  private final GitPlatformRepository gitPlatformRepository;
  private final GitProfileRepository gitProfileRepository;
  private final OrganizationRepository organizationRepository;

  private final UserMapper userMapper;

  public UserService(
    UserRepository userRepository,
    PersonRepository personRepository,
    UserMapper userMapper,
    GitPlatformRepository gitPlatformRepository,
    GitProfileRepository gitProfileRepository,
    OrganizationRepository organizationRepository
  ) {
    this.userRepository = userRepository;
    this.personRepository = personRepository;
    this.userMapper = userMapper;
    this.gitPlatformRepository = gitPlatformRepository;
    this.gitProfileRepository = gitProfileRepository;
    this.organizationRepository = organizationRepository;
  }

  public ReadUserDto getProfile(String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
    return userMapper.userToReadPersonDto(user);
  }

  public ReadUserDto createUser(String email, CreateUserDto createUserDto) throws ApiException {
    var optionalPerson = this.personRepository.findByEmail(email);
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "Email already taken");
    } else {
      var user = userMapper.createUserDtoToUser(createUserDto);
      user.setEmail(email);
      var gitPlatform = gitPlatformRepository
        .findById(createUserDto.getGitPlatform().toString())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Git platform not found"));
      var gitUser = GitProfile.builder().username(createUserDto.getGitProfileURI()).platform(gitPlatform).build();
      user.setGitProfile(gitUser);
      user.setRole(Role.builder().name("USER").build());
      gitProfileRepository.save(gitUser);
      userRepository.save(user);
      return userMapper.userToReadPersonDto(user);
    }
  }

  public void addWork(String email, String organizationId, WorkExperienceDto workExperienceDto) throws ApiException {
    var organization =
      this.organizationRepository.findById(organizationId)
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
}
