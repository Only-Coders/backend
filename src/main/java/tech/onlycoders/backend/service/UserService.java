package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.SortContactsBy;
import tech.onlycoders.backend.dto.SortUsersBy;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.user.GitPlatform;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.UpdateUserBlockedStatusDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.dto.user.response.ReadUserToDeleteDto;
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
import tech.onlycoders.backend.utils.GlobalVariables;
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
    ContactRequestMapper contactRequestMapper,
    Neo4jTemplate template
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

  public ReadUserDto getProfile(String sourceCanonicalName, String targetCanonicalName) throws ApiException {
    var partialUser =
      this.userRepository.findByCanonicalName(targetCanonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.profile-not-found"));
    var medals = this.userRepository.countUserMedals(targetCanonicalName);

    var followers = this.userRepository.countUserFollowers(targetCanonicalName);
    var contacts = this.userRepository.countContactsWithOutFilters(targetCanonicalName);

    var posts = this.postRepository.countUserPosts(targetCanonicalName);
    var currentPosition = this.workPositionRepository.getUserCurrentPositions(targetCanonicalName);
    var dto = userMapper.userToReadPersonDto(partialUser);
    dto.setMedalQty(medals);
    dto.setFollowerQty(followers);
    dto.setContactQty(contacts);
    dto.setPostQty(posts);
    if (partialUser.getGitProfile() != null) {
      dto.setGitProfile(
        GitProfileDto
          .builder()
          .userName(partialUser.getGitProfile().getUsername())
          .platform(GitPlatform.valueOf(partialUser.getGitProfile().getPlatform().getId()))
          .build()
      );
    }

    if (!sourceCanonicalName.equalsIgnoreCase(targetCanonicalName)) {
      Boolean isFollowing = this.userRepository.isFollowingAnotherUser(sourceCanonicalName, targetCanonicalName);
      Boolean isConnected = this.userRepository.areUsersConnected(sourceCanonicalName, targetCanonicalName);
      Boolean pendingRequest = this.userRepository.hasPendingRequest(sourceCanonicalName, targetCanonicalName);
      Boolean requestHasBeenSent = this.userRepository.requestHasBeenSent(sourceCanonicalName, targetCanonicalName);
      dto.setFollowing(isFollowing);
      dto.setConnected(isConnected);
      dto.setPendingRequest(pendingRequest);
      dto.setRequestHasBeenSent(requestHasBeenSent);
    }

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

    var contactRequest = ContactRequest.builder().message(contactRequestDto.getMessage()).build();

    contactRequestRepository.save(contactRequest);
    contactRequestRepository.setTarget(contactRequest.getId(), contact.getId());
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
    this.notificatorService.send(
        MessageDTO
          .builder()
          .message(user.getFullName() + " ha comenzado a seguirte!")
          .to(followed.getEmail())
          .eventType(EventType.NEW_FOLLOWER)
          .build()
      );
  }

  public void addFavoritePost(String email, String postId) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    var post =
      this.postRepository.getById(postId)
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

  public PaginateDto<ReadUserLiteDto> getMyFollows(
    String canonicalName,
    Integer page,
    Integer size,
    String partialName,
    String countryName,
    String skillName,
    SortContactsBy orderBy
  ) {
    var userRegex = "(?i)" + partialName + ".*";
    var countryRegex = "(?i)" + countryName + ".*";
    var skillNameRegex = "(?i)" + skillName + ".*";
    var users =
      this.userRepository.getMyFollows(
          canonicalName,
          page * size,
          size,
          userRegex,
          countryRegex,
          skillNameRegex,
          orderBy.label
        );
    var totalQuantity = this.userRepository.countFollows(canonicalName, userRegex, countryRegex, skillNameRegex);

    return getReadUserLiteDtoPaginateDto(page, size, users, totalQuantity);
  }

  public PaginateDto<ReadUserLiteDto> getMyContacts(
    String canonicalName,
    Integer page,
    Integer size,
    String partialName,
    String countryName,
    String skillName,
    SortContactsBy orderBy
  ) {
    var userRegex = "(?i)" + partialName + ".*";
    var countryRegex = "(?i)" + countryName + ".*";
    var skillNameRegex = "(?i)" + skillName + ".*";
    var users =
      this.userRepository.getMyContacts(
          canonicalName,
          page * size,
          size,
          userRegex,
          countryRegex,
          skillNameRegex,
          orderBy.label
        );
    var totalQuantity = this.userRepository.countContacts(canonicalName, userRegex, countryRegex, skillNameRegex);

    return getReadUserLiteDtoPaginateDto(page, size, users, totalQuantity);
  }

  public PaginateDto<ReadUserLiteDto> findByPartialName(
    Integer page,
    Integer size,
    String partialName,
    String countryName,
    String skillName,
    SortUsersBy orderBy
  ) {
    var regex = "(?i)" + CanonicalFactory.getCanonicalName(partialName) + ".*";
    var countryRegex = "(?i)" + countryName + ".*";
    var skillNameRegex = "(?i)" + skillName + ".*";
    var users =
      this.userRepository.findAllWithFilters(regex, countryRegex, skillNameRegex, orderBy.label, page * size, size);
    var totalQuantity = this.userRepository.countWithFilters(regex, countryRegex, skillNameRegex);
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

    paginated
      .getContent()
      .stream()
      .parallel()
      .forEach(
        user -> {
          var currentPosition = this.workPositionRepository.getUserCurrentPosition(user.getCanonicalName());
          var medals = this.userRepository.countUserMedals(user.getCanonicalName());
          user.setAmountOfMedals(medals);
          currentPosition.ifPresent(
            workPosition -> user.setCurrentPosition(workPositionMapper.workPositionToReadWorkPositionDto(workPosition))
          );
        }
      );

    return paginated;
  }

  public void acceptContactRequest(String email, ResponseContactRequestDto response) throws ApiException {
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));

    var requester =
      this.userRepository.findByCanonicalName(response.getRequesterCanonicalName())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    if (!contactRequestRepository.hasPendingRequest(requester.getId(), user.getId())) {
      throw new ApiException(HttpStatus.NOT_FOUND, "error.request-not-found");
    }

    if (response.getAcceptContact()) userRepository.addContact(email, response.getRequesterCanonicalName());
    contactRequestRepository.deleteRequest(requester.getId(), user.getId());
    userRepository.unfollowUser(requester.getId(), user.getId());
    userRepository.unfollowUser(user.getId(), requester.getId());
    this.notificatorService.send(
        MessageDTO
          .builder()
          .message(user.getFullName() + " ha aceptado tu peticion de contacto!")
          .to(requester.getEmail())
          .eventType(EventType.CONTACT_ACCEPTED)
          .build()
      );
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

  public ReadUserToDeleteDto setEliminationDate(String email) {
    var calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    calendar.add(Calendar.DAY_OF_YEAR, GlobalVariables.DAYS_TO_DELETE_USERS);
    var eliminationDate = calendar.getTime();
    this.userRepository.setEliminationDate(email, eliminationDate.getTime());
    return ReadUserToDeleteDto.builder().eliminationDate(eliminationDate).build();
  }

  public void removeContact(String requesterCanonicalName, String canonicalName) throws ApiException {
    var requesterUser =
      this.userRepository.findByCanonicalName(requesterCanonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));
    var targetUser =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    userRepository.removeContact(requesterUser.getId(), targetUser.getId());
  }

  public void cancelEliminationDate(String email) {
    this.userRepository.removeUserEliminationDate(email);
  }
}
