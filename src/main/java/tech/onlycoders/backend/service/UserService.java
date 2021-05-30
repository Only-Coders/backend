package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.UpdateUserBlockedStatusDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.ContactRequestMapper;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.model.ContactRequest;
import tech.onlycoders.backend.model.GitProfile;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PaginationUtils;
import tech.onlycoders.notificator.dto.EventType;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
@Transactional
public class UserService {

  private final WorkPositionRepository workPositionRepository;
  private final WorkPositionMapper workPositionMapper;
  private final UserRepository userRepository;
  private final PersonRepository personRepository;
  private final GitPlatformRepository gitPlatformRepository;
  private final CountryRepository countryRepository;
  private final PostRepository postRepository;
  private final RoleRepository roleRepository;
  private final ContactRequestRepository contactRequestRepository;

  private final AuthService authService;
  private final NotificatorService notificatorService;

  private final UserMapper userMapper;
  private final PostMapper postMapper;
  private final ContactRequestMapper contactRequestMapper;

  public UserService(
    WorkPositionRepository workPositionRepository,
    WorkPositionMapper workPositionMapper,
    UserRepository userRepository,
    PersonRepository personRepository,
    UserMapper userMapper,
    GitPlatformRepository gitPlatformRepository,
    CountryRepository countryRepository,
    AuthService authService,
    PostRepository postRepository,
    RoleRepository roleRepository,
    ContactRequestRepository contactRequestRepository,
    NotificatorService notificatorService,
    PostMapper postMapper,
    ContactRequestMapper contactRequestMapper
  ) {
    this.workPositionRepository = workPositionRepository;
    this.workPositionMapper = workPositionMapper;
    this.userRepository = userRepository;
    this.personRepository = personRepository;
    this.userMapper = userMapper;
    this.gitPlatformRepository = gitPlatformRepository;
    this.countryRepository = countryRepository;
    this.authService = authService;
    this.postRepository = postRepository;
    this.roleRepository = roleRepository;
    this.contactRequestRepository = contactRequestRepository;
    this.notificatorService = notificatorService;
    this.postMapper = postMapper;
    this.contactRequestMapper = contactRequestMapper;
  }

  public ReadUserDto getProfile(String canonicalName) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.profile-not-found"));
    var medals = this.userRepository.countUserMedals(canonicalName);
    var followers = this.userRepository.countUserFollowers(canonicalName);
    var contacts = this.userRepository.countContacts(canonicalName);
    var posts = this.postRepository.countUserPosts(canonicalName);
    var currentPosition = this.workPositionRepository.getUserCurrentPositions(canonicalName);
    var dto = userMapper.userToReadPersonDto(user);
    dto.setMedalQty(medals);
    dto.setFollowerQty(followers);
    dto.setContactQty(contacts);
    dto.setPostQty(posts);
    if (!currentPosition.isEmpty()) {
      var workPositionDto = this.workPositionMapper.workPositionToReadWorkPositionDto(currentPosition.get(0));
      dto.setCurrentPosition(workPositionDto);
    }
    return dto;
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
    contactRequestRepository.createSendContactRequest(contactRequest.getId(), user.getId());

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

  public PaginateDto<ReadContactRequestDto> getReceivedContactRequests(String email, Integer page, Integer size)
    throws ApiException {
    var totalQuantity = this.contactRequestRepository.getReceivedContactRequestTotalQuantity(email);
    if (totalQuantity == 0) {
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    }
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var contactRequests = this.contactRequestRepository.getReceivedContactRequests(email, page * size, size);

    var requestDtos = contactRequestMapper.contactRequestListToReadContactRequestDtoList(contactRequests);
    for (ReadContactRequestDto request : requestDtos) {
      var position = workPositionRepository.getUserCurrentPositions(request.getRequester().getCanonicalName());
      if (position.size() > 0) {
        request
          .getRequester()
          .setCurrentPosition(workPositionMapper.workPositionToReadWorkPositionDto(position.get(0)));
      }
    }

    var paginated = new PaginateDto<ReadContactRequestDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    paginated.setContent(requestDtos);

    return paginated;
  }

  public PaginateDto<ReadUserLiteDto> getMyContacts(String canonicalName, Integer page, Integer size) {
    var users = this.userRepository.getContacts(canonicalName);

    var totalQuantity = this.userRepository.countContacts(canonicalName);
    return getReadUserLiteDtoPaginateDto(page, size, users, totalQuantity);
  }

  public PaginateDto<ReadUserLiteDto> findByPartialName(String partialName, Integer page, Integer size) {
    var regex = "(?i)" + CanonicalFactory.getCanonicalName(partialName) + ".*";
    var users = this.userRepository.findByPartialName(regex, page, size);
    var totalQuantity = this.userRepository.countByPartialName(regex);
    return getReadUserLiteDtoPaginateDto(page, size, users, totalQuantity);
  }

  private PaginateDto<ReadUserLiteDto> getReadUserLiteDtoPaginateDto(
    Integer page,
    Integer size,
    List<User> users,
    Integer totalQuantity
  ) {
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var paginated = new PaginateDto<ReadUserLiteDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    paginated.setContent(userMapper.listUserToListReadUserLiteDto(users));

    return paginated;
  }

  public void responseContactRequest(String email, ResponseContactRequestDto response) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));

    var requester =
      this.userRepository.findByCanonicalName(response.getRequesterCanonicalName())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    if (!contactRequestRepository.hasPendingRequest(requester.getId(), user.getId())) throw new ApiException(
      HttpStatus.NOT_FOUND,
      "error.request-not-found"
    );

    if (response.getAcceptContact()) userRepository.addContact(email, response.getRequesterCanonicalName());
    contactRequestRepository.deleteRequest(requester.getId(), user.getId());
  }

  public void removeFavoritePost(String email, String postId) {
    this.userRepository.removeFavoritePost(email, postId);
  }

  public void setUserBlockedStatus(String canonicalName, UpdateUserBlockedStatusDto blockedStatusDto)
    throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    this.userRepository.setBlockedStatus(user.getId(), blockedStatusDto.getBlocked());
  }
}
