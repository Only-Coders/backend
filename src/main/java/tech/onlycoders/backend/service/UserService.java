package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.GitProfile;
import tech.onlycoders.backend.repository.GitPlatformRepository;
import tech.onlycoders.backend.repository.GitProfileRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PersonRepository personRepository;
  private final GitPlatformRepository gitPlatformRepository;
  private final UserMapper userMapper;
  private final GitProfileRepository gitProfileRepository;

  public UserService(
    UserRepository userRepository,
    PersonRepository personRepository,
    UserMapper userMapper,
    GitPlatformRepository gitPlatformRepository,
    GitProfileRepository gitProfileRepository
  ) {
    this.userRepository = userRepository;
    this.personRepository = personRepository;
    this.userMapper = userMapper;
    this.gitPlatformRepository = gitPlatformRepository;
    this.gitProfileRepository = gitProfileRepository;
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
      gitProfileRepository.save(gitUser);
      userRepository.save(user);
      return userMapper.userToReadPersonDto(user);
    }
  }
}
