package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
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
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PaginationUtils;
import tech.onlycoders.notificator.dto.EventType;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
@Transactional
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
  private final ContactRequestRepository contactRequestRepository;
  private final WorkPositionRepository workPositionRepository;
  private final DegreeRepository degreeRepository;

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
    WorkPositionRepository workPositionRepository,
    AuthService authService,
    TagRepository tagRepository,
    PostRepository postRepository,
    RoleRepository roleRepository,
    ContactRequestRepository contactRequestRepository,
    DegreeRepository degreeRepository,
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
    this.workPositionRepository = workPositionRepository;
    this.authService = authService;
    this.tagRepository = tagRepository;
    this.postRepository = postRepository;
    this.roleRepository = roleRepository;
    this.contactRequestRepository = contactRequestRepository;
    this.degreeRepository = degreeRepository;
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
      var role = roleRepository
        .findById("USER")
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));

      var country = countryRepository
        .findById(createUserDto.getCountry().getCode())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.country-not-found"));

      var user = userMapper.createUserDtoToUser(createUserDto);
      user.setEmail(email);
      user.setCountry(country);

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
    this.workPositionRepository.save(workPosition);
    this.workPositionRepository.addUserWorkPosition(workPosition.getId(), user.getId());
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
    var degree = new Degree();
    degree.setInstitute(institute);
    degree.setSince(educationExperienceDto.getSince());
    degree.setUntil(educationExperienceDto.getUntil());
    degree.setDegree(educationExperienceDto.getDegree());
    degreeRepository.save(degree);
    degreeRepository.storeDegree(degree.getId(), user.getId());
    return educationExperienceDto;
  }

  public List<ReadUserLiteDto> getSuggestedUsers(String email, Integer size) {
    var users = userRepository.findSuggestedUsers(email, size);
    var userDtos = userMapper.listUserToListReadUserLiteDto(new ArrayList<>(users));

    for (ReadUserLiteDto user : userDtos) {
      var currentPositions = this.workPositionRepository.getUserCurrentPositions(user.canonicalName);
      if (currentPositions.size() > 0) {
        user.setCurrentPosition(workPositionMapper.workPositionToReadWorkPositionDto(currentPositions.get(0)));
      }
    }

    return userDtos;
  }

  public void addSkill(String email, String canonicalName) throws ApiException {
    var skill =
      this.skillRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    this.userRepository.addSkill(user.getId(), skill.getCanonicalName());
  }

  public void addTag(String email, String canonicalName) throws ApiException {
    var tag =
      this.tagRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.tag-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    this.userRepository.followTag(user.getId(), tag.getCanonicalName());
  }

  public void sendContactRequest(String email, CreateContactRequestDto contactRequestDto) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    var contact =
      this.userRepository.findByCanonicalName(contactRequestDto.getCanonicalName())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    if (contactRequestRepository.hasPendingRequest(user.getId(), contact.getId())) {
      throw new ApiException(HttpStatus.CONFLICT, "error.pending-request");
    }

    var contactRequest = ContactRequest.builder().message(contactRequestDto.getMessage()).target(contact).build();

    contactRequestRepository.save(contactRequest);
    contactRequestRepository.storeContactRequest(contactRequest.getId(), user.getId());

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
    this.userRepository.followUser(user.getId(), followed.getId());
  }

  public void addFavoritePost(String email, String postId) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    var post =
      this.postRepository.findById(postId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));
    this.userRepository.addFavoritePost(user.getId(), post.getId());
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

  public void unfollowUser(String email, String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));
    var followed =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    this.userRepository.unfollowUser(user.getId(), followed.getId());
  }

  public void deleteContactRequest(String email, String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    var targetUser =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    contactRequestRepository.deleteRequest(user.getId(), targetUser.getId());
  }
}
