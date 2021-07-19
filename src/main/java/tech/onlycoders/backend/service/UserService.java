package tech.onlycoders.backend.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.SortContactsBy;
import tech.onlycoders.backend.dto.SortUsersBy;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.dto.language.request.UpdateUserLanguageDto;
import tech.onlycoders.backend.dto.language.response.ReadLanguageDto;
import tech.onlycoders.backend.dto.user.GitPlatform;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.user.request.*;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.dto.user.response.ReadUserToDeleteDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.ContactRequestMapper;
import tech.onlycoders.backend.mapper.LanguageMapper;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.model.*;
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
  private final LanguageRepository languageRepository;
  private final BlacklistRepository blacklistRepository;
  private final FirebaseService firebaseService;

  private final AuthService authService;
  private final NotificatorService notificatorService;

  private final UserMapper userMapper;
  private final ContactRequestMapper contactRequestMapper;
  private final LanguageMapper languageMapper;
  private final FCMTokenRepository fcmTokenRepository;

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
    ContactRequestMapper contactRequestMapper,
    LanguageRepository languageRepository,
    BlacklistRepository blacklistRepository,
    FirebaseService firebaseService,
    LanguageMapper languageMapper,
    FCMTokenRepository fcmTokenRepository
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
    this.contactRequestMapper = contactRequestMapper;
    this.languageRepository = languageRepository;
    this.firebaseService = firebaseService;
    this.languageMapper = languageMapper;
    this.blacklistRepository = blacklistRepository;
    this.fcmTokenRepository = fcmTokenRepository;
  }

  public ReadUserDto getProfile(String sourceCanonicalName, String targetCanonicalName) throws ApiException {
    var partialUser =
      this.userRepository.findByCanonicalName(targetCanonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.profile-not-found"));
    var medals = this.userRepository.countUserMedals(targetCanonicalName);

    var followers = this.userRepository.countUserFollowers(targetCanonicalName);
    var following = this.userRepository.countUserFollowing(targetCanonicalName);
    var contacts = this.userRepository.countContactsWithOutFilters(targetCanonicalName);

    var posts = this.postRepository.countUserPosts(targetCanonicalName);
    var currentPosition = this.workPositionRepository.getUserCurrentPositions(targetCanonicalName);
    var dto = userMapper.partialUserToReadPersonDto(partialUser);
    dto.setMedalQty(medals);
    dto.setFollowerQty(followers);
    dto.setFollowingQty(following);
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
    // TODO: we should verify if the user is in the blacklist
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

      user.setConfigs(
        Arrays
          .stream(EventType.values())
          .filter(eventType -> eventType != EventType.NEW_ADMIN_ACCOUNT)
          .map(eventType -> NotificationConfig.builder().push(true).email(true).type(eventType).build())
          .collect(Collectors.toSet())
      );

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
      var lang = Language.builder().code("en").name("English").build();
      user.setLanguage(lang);
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
        MessageDTO
          .builder()
          .message(message)
          .from(user.getEmail())
          .to(contact.getEmail())
          .eventType(EventType.CONTACT_REQUEST)
          .build()
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
          .from(user.getEmail())
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
    var userRegex = "(?i).*" + partialName + ".*";
    var countryRegex = "(?i).*" + countryName + ".*";
    var skillNameRegex = "(?i).*" + skillName + ".*";

    List<User> users;
    if (orderBy == SortContactsBy.MEDALS) {
      users =
        this.userRepository.getMyFollowsSortByMedals(
            canonicalName,
            page * size,
            size,
            userRegex,
            countryRegex,
            skillNameRegex
          );
    } else {
      users =
        this.userRepository.getMyFollows(
            canonicalName,
            page * size,
            size,
            userRegex,
            countryRegex,
            skillNameRegex,
            orderBy.label
          );
    }
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
    var userRegex = "(?i).*" + partialName + ".*";
    var countryRegex = "(?i).*" + countryName + ".*";
    var skillNameRegex = "(?i).*" + skillName + ".*";
    List<User> users;
    if (orderBy == SortContactsBy.MEDALS) {
      users =
        this.userRepository.getMyContactsSortByMedals(
            canonicalName,
            page * size,
            size,
            userRegex,
            countryRegex,
            skillNameRegex
          );
    } else {
      users =
        this.userRepository.getMyContacts(
            canonicalName,
            page * size,
            size,
            userRegex,
            countryRegex,
            skillNameRegex,
            orderBy.label
          );
    }
    var totalQuantity = this.userRepository.countContacts(canonicalName, userRegex, countryRegex, skillNameRegex);

    return getReadUserLiteDtoPaginateDto(page, size, users, totalQuantity);
  }

  public PaginateDto<ReadUserDto> findByPartialName(
    String canonicalName,
    Integer page,
    Integer size,
    String partialName,
    String countryName,
    String skillName,
    SortUsersBy orderBy
  ) {
    var regex = "(?i).*" + CanonicalFactory.getCanonicalName(partialName) + ".*";
    var countryRegex = "(?i).*" + countryName + ".*";
    var skillNameRegex = "(?i).*" + skillName + ".*";

    List<User> users;
    if (orderBy == SortUsersBy.MEDALS) {
      users =
        this.userRepository.findAllWithFiltersAndSortByMedals(regex, countryRegex, skillNameRegex, page * size, size);
    } else {
      var field = orderBy.label;
      users = this.userRepository.findAllWithFilters(regex, countryRegex, skillNameRegex, field, page * size, size);
    }
    var totalQuantity = this.userRepository.countWithFilters(regex, countryRegex, skillNameRegex);
    var dtos = getReadUserDtoPaginateDto(page, size, users, totalQuantity);

    dtos
      .getContent()
      .stream()
      .parallel()
      .forEach(
        user -> {
          Boolean isFollowing = this.userRepository.isFollowingAnotherUser(canonicalName, user.getCanonicalName());
          Boolean isConnected = this.userRepository.areUsersConnected(canonicalName, user.getCanonicalName());
          Boolean pendingRequest = this.userRepository.hasPendingRequest(canonicalName, user.getCanonicalName());
          Boolean requestHasBeenSent = this.userRepository.requestHasBeenSent(canonicalName, user.getCanonicalName());
          user.setFollowing(isFollowing);
          user.setConnected(isConnected);
          user.setPendingRequest(pendingRequest);
          user.setRequestHasBeenSent(requestHasBeenSent);
        }
      );
    return dtos;
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

  private PaginateDto<ReadUserDto> getReadUserDtoPaginateDto(
    Integer page,
    Integer size,
    List<User> users,
    Integer totalQuantity
  ) {
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var paginated = new PaginateDto<ReadUserDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    paginated.setContent(userMapper.listUserToListReadUserDto(users));

    paginated
      .getContent()
      .stream()
      .parallel()
      .forEach(
        user -> {
          var currentPosition = this.workPositionRepository.getUserCurrentPosition(user.getCanonicalName());
          var medals = this.userRepository.countUserMedals(user.getCanonicalName());
          user.setMedalQty(medals);
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

    if (response.getAcceptContact()) {
      userRepository.addContact(email, response.getRequesterCanonicalName());
      userRepository.unfollowUser(requester.getId(), user.getId());
      userRepository.unfollowUser(user.getId(), requester.getId());
    }

    contactRequestRepository.deleteRequest(requester.getId(), user.getId());
    this.notificatorService.send(
        MessageDTO
          .builder()
          .message(user.getFullName() + " ha aceptado tu peticion de contacto!")
          .from(user.getEmail())
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
    return ReadUserToDeleteDto.builder().eliminationDate(eliminationDate.getTime()).build();
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

  public ReadUserDto updateProfile(String canonicalName, UpdateUserDto updateUserDto) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));

    Long birthDate = null;
    if (updateUserDto.getBirthDate() != null) {
      birthDate = updateUserDto.getBirthDate().toInstant().toEpochMilli();
    }
    userRepository.updateProfile(
      canonicalName,
      birthDate,
      updateUserDto.getDescription(),
      updateUserDto.getFirstName(),
      updateUserDto.getLastName(),
      updateUserDto.getImageURI(),
      updateUserDto.getFirstName() + " " + updateUserDto.getLastName()
    );

    if (!user.getCountry().getCode().equals(updateUserDto.getCountryCode())) {
      countryRepository
        .findById(updateUserDto.getCountryCode())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.country-not-found"));
      userRepository.setCountry(canonicalName, updateUserDto.getCountryCode());
    }

    if (updateUserDto.getGitProfile() != null && user.getGitProfile() == null) {
      //Se agrego el usuario de git
      var gitPlatform = gitPlatformRepository
        .findById(updateUserDto.getGitProfile().getPlatform().toString())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.git-platform-not-found"));
      userRepository.setGitProfile(canonicalName, updateUserDto.getGitProfile().getUserName(), gitPlatform.getId());
    } else if (updateUserDto.getGitProfile() == null) {
      //Se elimino el usuario de git
      userRepository.removeGitProfile(canonicalName);
    } else if (
      !user.getGitProfile().getPlatform().getId().equals(updateUserDto.getGitProfile().getPlatform().toString())
    ) {
      //Se cambio la platafoma de git
      var gitPlatform = gitPlatformRepository
        .findById(updateUserDto.getGitProfile().getPlatform().toString())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.git-platform-not-found"));
      userRepository.setGitProfile(canonicalName, updateUserDto.getGitProfile().getUserName(), gitPlatform.getId());
    } else if (!user.getGitProfile().getUsername().equals(updateUserDto.getGitProfile().getUserName())) {
      //Se cambio el nombre de usuario de git
      userRepository.updateGitProfile(canonicalName, updateUserDto.getGitProfile().getUserName());
    }
    user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));

    return this.getProfile(user.getCanonicalName(), user.getCanonicalName());
  }

  public ReadLanguageDto getUserLanguage(String canonicalName) {
    return languageMapper.LanguageToReadLanguageDto(languageRepository.getUserLanguage(canonicalName));
  }

  public void setUserLanguage(String canonicalName, UpdateUserLanguageDto languageDto) throws ApiException {
    var lang = languageRepository
      .findById(languageDto.getCode())
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.language-not-found"));
    languageRepository.setUserLanguage(canonicalName, lang.getCode());
  }

  public void deleteAndBanUser(String canonicalName) throws ApiException {
    var user = userRepository
      .findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var ban = BlackList.builder().email(user.getEmail()).build();
    blacklistRepository.save(ban);
    firebaseService.deleteAccount(user.getEmail());
    userRepository.deleteUser(user.getEmail());
  }

  public void addFCMToken(String canonicalName, AddFCMTokenDto addFCMToken) {
    this.fcmTokenRepository.findByDeviceId(addFCMToken.getDeviceId())
      .ifPresentOrElse(
        fcmToken -> {
          fcmToken.setToken(addFCMToken.getFcmToken());
          this.fcmTokenRepository.save(fcmToken);
        },
        () -> {
          var fcmToken = FCMToken
            .builder()
            .token(addFCMToken.getFcmToken())
            .deviceId(addFCMToken.getDeviceId())
            .build();
          this.fcmTokenRepository.save(fcmToken);
          this.fcmTokenRepository.addUserToken(canonicalName, fcmToken.getId());
        }
      );
  }

  public ReadUserDto patchUserImage(String canonicalName, PatchUserImageDto patchUserImageDto) throws ApiException {
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.user-not-found"));

    userRepository.updateUserImage(user.getId(), patchUserImageDto.getImageURI());
    return this.getProfile(user.getCanonicalName(), user.getCanonicalName());
  }

  public void deleteFCMToken(String deviceId, String canonicalName) throws ApiException {
    var fcmToken =
      this.fcmTokenRepository.findByDeviceId(deviceId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.device-not-found"));
    var tokenBelongToUser = this.fcmTokenRepository.verifyIfTokenBelongsToUser(fcmToken.getId(), canonicalName);
    if (tokenBelongToUser) {
      this.fcmTokenRepository.delete(fcmToken);
    } else {
      throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
    }
  }
}
