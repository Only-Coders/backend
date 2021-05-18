package tech.onlycoders.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.notificator.EventType;
import tech.onlycoders.backend.dto.notificator.MessageDTO;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.model.ContactRequest;
import tech.onlycoders.backend.model.Degree;
import tech.onlycoders.backend.model.GitProfile;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PersonRepository personRepository;
  private final GitPlatformRepository gitPlatformRepository;
  private final WorkplaceRepository workplaceRepository;
  private final InstituteRepository instituteRepository;
  private final CountryRepository countryRepository;
  private final SkillRepository skillRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final RoleRepository roleRepository;

  private final AuthService authService;
  private final NotificatorService notificatorService;

  private final UserMapper userMapper;
  private final PostMapper postMapper;
  private final WorkPositionMapper workPositionMapper;

  public UserService(
    UserRepository userRepository,
    PersonRepository personRepository,
    UserMapper userMapper,
    GitPlatformRepository gitPlatformRepository,
    WorkplaceRepository workplaceRepository,
    InstituteRepository instituteRepository,
    CountryRepository countryRepository,
    SkillRepository skillRepository,
    AuthService authService,
    TagRepository tagRepository,
    PostRepository postRepository,
    RoleRepository roleRepository,
    NotificatorService notificatorService,
    PostMapper postMapper,
    WorkPositionMapper workPositionMapper
  ) {
    this.userRepository = userRepository;
    this.personRepository = personRepository;
    this.userMapper = userMapper;
    this.gitPlatformRepository = gitPlatformRepository;
    this.workplaceRepository = workplaceRepository;
    this.instituteRepository = instituteRepository;
    this.countryRepository = countryRepository;
    this.skillRepository = skillRepository;
    this.authService = authService;
    this.tagRepository = tagRepository;
    this.postRepository = postRepository;
    this.roleRepository = roleRepository;
    this.notificatorService = notificatorService;
    this.postMapper = postMapper;
    this.workPositionMapper = workPositionMapper;
  }

  public ReadUserDto getProfile(String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.profile-not-found"));
    return userMapper.userToReadPersonDto(user);
  }

  public AuthResponseDto createUser(String email, CreateUserDto createUserDto) throws ApiException {
    var optionalPerson = this.personRepository.findByEmail(email);
    if (optionalPerson.isPresent()) {
      throw new ApiException(HttpStatus.CONFLICT, "error.email-taken");
    } else {
      var user = userMapper.createUserDtoToUser(createUserDto);
      var role = roleRepository
        .findById("USER")
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
      user.country =
        countryRepository
          .findById(createUserDto.getCountry().getCode())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.country-not-found"));
      user.setEmail(email);
      if (createUserDto.getGitProfile() != null) {
        var gitPlatform = gitPlatformRepository
          .findById(createUserDto.getGitProfile().getPlatform().toString())
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.git-platform-not-found"));
        var gitProfile = GitProfile
          .builder()
          .username(createUserDto.getGitProfile().getUserName())
          .platform(gitPlatform)
          .build();
        user.setGitProfile(gitProfile);
      }
      user.setRole(role);
      this.userRepository.save(user);
      return this.authService.postCreateUser(user);
    }
  }

  public ReadWorkPositionDto addWork(String email, WorkExperienceDto workExperienceDto) throws ApiException {
    var workplace =
      this.workplaceRepository.findById(workExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.workplace-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var workPosition = new WorkPosition();
    workPosition.setWorkplace(workplace);
    workPosition.setSince(workExperienceDto.getSince());
    workPosition.setUntil(workExperienceDto.getUntil());
    workPosition.setPosition(workExperienceDto.getPosition());
    user.getWorkingPlaces().add(workPosition);
    this.userRepository.save(user);
    return this.workPositionMapper.workPositionToReadWorkPositionDto(workPosition);
  }

  public EducationExperienceDto addSchool(String email, EducationExperienceDto educationExperienceDto)
    throws ApiException {
    var institute =
      this.instituteRepository.findById(educationExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.workplace-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var studiesAt = new Degree();
    studiesAt.setInstitute(institute);
    studiesAt.setSince(educationExperienceDto.getSince());
    studiesAt.setUntil(educationExperienceDto.getUntil());
    studiesAt.setDegree(educationExperienceDto.getDegree());
    user.getSchools().add(studiesAt);
    this.userRepository.save(user);
    return educationExperienceDto;
  }

  public List<ReadUserLiteDto> getSuggestedUsers(String email, Integer size) {
    var users = userRepository.findSuggestedUsers(email, size);
    return userMapper.listUserToListReadUserLiteDto(users);
  }

  public void addSkill(String email, String canonicalName) throws ApiException {
    var skill =
      this.skillRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.skill-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    user.getSkills().add(skill);
    this.userRepository.save(user);
  }

  public void addTag(String email, String canonicalName) throws ApiException {
    var tag =
      this.tagRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.tag-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    user.getTags().add(tag);
    this.userRepository.save(user);
  }

  public void sendContactRequest(String email, CreateContactRequestDto contactRequestDto) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var contact =
      this.userRepository.findByCanonicalName(contactRequestDto.getCanonicalName())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var contactRequest = ContactRequest.builder().message(contactRequestDto.getMessage()).receiver(contact).build();
    user.getRequests().add(contactRequest);
    userRepository.save(user);

    var message = String.format(
      "%s %s te ha enviado una solicitud de contacto!",
      user.getFirstName(),
      user.getLastName()
    );
    this.notificatorService.send(
        MessageDTO.builder().message(message).to(contact.getEmail()).eventType(EventType.CONTACT_REQUEST).build()
      );
  }

  public void followUser(String email, String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));
    var followed =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    user.getFollowed().add(followed);
    userRepository.save(user);
  }

  public void addFavoritePost(String email, String postId) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    var post =
      this.postRepository.findById(postId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));

    post.getUserFavorites().add(user);
    postRepository.save(post);
  }

  public PaginateDto<ReadPostDto> getFavoritePosts(String email, Integer page, Integer size) throws ApiException {
    var totalQuantity = this.postRepository.getUserFavoritePostTotalQuantity(email);
    if (totalQuantity == 0) {
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    }
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var posts = this.postRepository.getUserFavoritePosts(email, page * size, size);

    var paginated = new PaginateDto<ReadPostDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    paginated.setContent(postMapper.listPostToListPostDto(posts));

    return paginated;
  }
}
