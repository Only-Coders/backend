package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tech.onlycoders.backend.dto.SortContactsBy;
import tech.onlycoders.backend.dto.SortUsersBy;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.contactrequest.request.ResponseContactRequestDto;
import tech.onlycoders.backend.dto.language.request.UpdateUserLanguageDto;
import tech.onlycoders.backend.dto.user.GitProfileDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.request.UpdateUserBlockedStatusDto;
import tech.onlycoders.backend.dto.user.request.UpdateUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.*;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PartialGitProfileImpl;
import tech.onlycoders.backend.utils.PartialPostImpl;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  private UserService service;

  @Mock
  private NotificatorService notificatorService;

  @Mock
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private WorkplaceRepository workplaceRepository;

  @Mock
  private InstituteRepository instituteRepository;

  @Mock
  private GitPlatformRepository gitPlatformRepository;

  @Mock
  private GitProfileRepository gitProfileRepository;

  @Mock
  private DegreeRepository degreeRepository;

  @Mock
  private WorkPositionRepository workPositionRepository;

  @Mock
  private ContactRequestRepository contactRequestRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private CountryRepository countryRepository;

  @Mock
  private SkillRepository skillRepository;

  @Mock
  private LanguageRepository languageRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Mock
  private TagRepository tagRepository;

  @Mock
  private PostRepository postRepository;

  @Spy
  private final UserMapper userMapper = new UserMapperImpl();

  @Spy
  private final WorkPositionMapper workPositionMapper = new WorkPositionMapperImpl(new WorkplaceMapperImpl());

  @Spy
  private final PostMapper postMapper = new PostMapperImpl(new TagMapperImpl());

  @Spy
  private final ContactRequestMapper contactRequestMapper = new ContactRequestMapperImpl();

  @Spy
  private final LanguageMapper languageMapper = Mappers.getMapper(LanguageMapper.class);

  @Test
  public void ShouldFailWhenFirebaseReturnsException() {
    var canonicalName = ezRandom.nextObject(String.class);
    var sourceCanonicalName = ezRandom.nextObject(String.class);
    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());
    assertThrows(ApiException.class, () -> this.service.getProfile(sourceCanonicalName, canonicalName));
  }

  @Test
  public void ShouldReturnUserProfile() throws ApiException {
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var sourceCanonicalName = ezRandom.nextObject(String.class);
    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito
      .when(this.userRepository.countContactsWithOutFilters(user.getCanonicalName()))
      .thenReturn(ezRandom.nextInt());
    Mockito.when(this.userRepository.countUserFollowers(user.getCanonicalName())).thenReturn(ezRandom.nextInt());
    Mockito.when(this.userRepository.countUserMedals(user.getCanonicalName())).thenReturn(ezRandom.nextInt());
    Mockito.when(this.postRepository.countUserPosts(user.getCanonicalName())).thenReturn(ezRandom.nextInt());
    Mockito
      .when(this.workPositionRepository.getUserCurrentPositions(user.getCanonicalName()))
      .thenReturn(ezRandom.objects(WorkPosition.class, 10).collect(Collectors.toList()));
    var profile = this.service.getProfile(sourceCanonicalName, user.getCanonicalName());
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
    Mockito.when(this.roleRepository.findById(anyString())).thenReturn(Optional.of(ezRandom.nextObject(Role.class)));
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
  public void ShouldFollowUser() throws ApiException {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.of(user2));

    this.service.followUser(email, cName);
  }

  @Test
  public void ShouldFailFollowUserWhenWrongEmail() {
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.followUser(email, cName));
  }

  @Test
  public void ShouldFailFollowUserWhenWrongCanonicalName() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.followUser(email, cName));
  }

  @Test
  public void ShouldReturnSuggestedUsers() {
    var email = ezRandom.nextObject(String.class);
    var users = new HashSet<User>();
    users.add(ezRandom.nextObject(User.class));

    var workPositions = new ArrayList<WorkPosition>();
    workPositions.add(ezRandom.nextObject(WorkPosition.class));

    Mockito.when(this.userRepository.findSuggestedUsers(anyString(), anyInt())).thenReturn(users);
    Mockito.when(this.workPositionRepository.getUserCurrentPositions(anyString())).thenReturn(workPositions);
    var userLiteDtos = this.service.getSuggestedUsers(email, 1);
    assertNotNull(userLiteDtos);
  }

  @Test
  public void ShouldSendRequest() throws ApiException {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.of(user2));

    this.service.sendContactRequest(email, reqDto);
  }

  @Test
  public void ShouldFailWithConflictWhenSendContactRequest() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.of(user2));
    Mockito.when(this.contactRequestRepository.hasPendingRequest(user1.getId(), user2.getId())).thenReturn(true);

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(user1.getEmail(), reqDto));
  }

  @Test
  public void ShouldFailSendRequestUserWhenWrongEmail() {
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(email, reqDto));
  }

  @Test
  public void ShouldFailSendRequestUserWhenCanonicalName() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(email, reqDto));
  }

  @Test
  public void ShouldAddFavoritePost() throws ApiException {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var post = new PartialPostImpl();
    var email = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.postRepository.getById(postId)).thenReturn(Optional.of(post));

    this.service.addFavoritePost(email, postId);
  }

  @Test
  public void ShouldFailAddFavoritePostWhenWrongEmail() {
    var email = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addFavoritePost(email, postId));
  }

  @Test
  public void ShouldFailAddFavoritePostWhenWrongPostId() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.postRepository.getById(postId)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addFavoritePost(email, postId));
  }

  @Test
  public void ShouldUnfollowUser() throws ApiException {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.of(user2));

    this.service.unfollowUser(email, cName);
  }

  @Test
  public void ShouldFailUnfollowUserWhenWrongEmail() {
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.unfollowUser(email, cName));
  }

  @Test
  public void ShouldFailUnfollowUserWhenWrongCanonicalName() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.unfollowUser(email, cName));
  }

  @Test
  public void ShouldUnSendRequest() throws ApiException {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);

    Mockito.when(this.userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(user2.getCanonicalName())).thenReturn(Optional.of(user2));

    this.service.deleteContactRequest(user1.getEmail(), user2.getCanonicalName());
  }

  @Test
  public void ShouldFailUnSendRequestUserWhenWrongEmail() {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
    Mockito.when(this.userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.empty());

    assertThrows(
      ApiException.class,
      () -> this.service.deleteContactRequest(user1.getEmail(), user2.getCanonicalName())
    );
  }

  @Test
  public void ShouldFailUnSendRequestUserWhenCanonicalName() {
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);

    Mockito.when(this.userRepository.findByEmail(user1.getEmail())).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(user2.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(
      ApiException.class,
      () -> this.service.deleteContactRequest(user1.getEmail(), user2.getCanonicalName())
    );
  }

  @Test
  public void ShouldReturnContactRequests() throws ApiException {
    var email = "email";

    var request = new ContactRequest();
    request.setRequester(ezRandom.nextObject(User.class));
    var list = new ArrayList<ContactRequest>();
    list.add(request);

    var position = ezRandom.nextObject(WorkPosition.class);
    position.setWorkplace(ezRandom.nextObject(Workplace.class));
    var positionList = new ArrayList<WorkPosition>();
    positionList.add(position);

    Mockito.when(this.contactRequestRepository.getReceivedContactRequestTotalQuantity(anyString())).thenReturn(1);
    Mockito
      .when(this.contactRequestRepository.getReceivedContactRequests(anyString(), anyInt(), anyInt()))
      .thenReturn(list);
    Mockito.when(this.workPositionRepository.getUserCurrentPositions(anyString())).thenReturn(positionList);

    var res = this.service.getReceivedContactRequests(email, 0, 10);
    assertNotNull(res);
  }

  @Test
  public void ShouldFailReturnContactRequestsWhenUserNotFound() {
    var email = "email";

    var request = new ContactRequest();
    request.setRequester(ezRandom.nextObject(User.class));

    Mockito.when(this.contactRequestRepository.getReceivedContactRequestTotalQuantity(anyString())).thenReturn(0);
    Mockito.when(this.userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.getReceivedContactRequests(email, 0, 10));
  }

  @Test
  public void ShouldGetContactsOfUser() {
    var canonicalName = ezRandom.nextObject(String.class);
    var usersList = ezRandom.objects(User.class, 10).collect(Collectors.toList());
    var size = 20;
    var page = 0;

    Mockito
      .when(
        this.userRepository.getMyContacts(
            canonicalName,
            page * size,
            size,
            "(?i).*",
            "(?i).*",
            "(?i).*",
            SortContactsBy.FULLNAME.label
          )
      )
      .thenReturn(usersList);
    Mockito.when(this.userRepository.countContacts(canonicalName, "(?i).*", "(?i).*", "(?i).*")).thenReturn(1);
    Mockito
      .when(this.workPositionRepository.getUserCurrentPosition(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(WorkPosition.class)));

    var result = this.service.getMyContacts(canonicalName, page, size, "", "", "", SortContactsBy.FULLNAME);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldAcceptContactRequest() throws ApiException {
    var response = new ResponseContactRequestDto();
    response.setRequesterCanonicalName("ds");
    response.setAcceptContact(true);
    var user1 = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);

    Mockito.when(this.userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user2));
    Mockito.when(this.contactRequestRepository.hasPendingRequest(user2.getId(), user1.getId())).thenReturn(true);

    this.service.acceptContactRequest("email", response);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldRejectContactRequest() throws ApiException {
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var response = new ResponseContactRequestDto();
    response.setRequesterCanonicalName(user.getCanonicalName());
    response.setAcceptContact(false);

    Mockito.when(this.userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));
    Mockito.when(this.userRepository.findByEmail(user2.getEmail())).thenReturn(Optional.of(user2));
    Mockito.when(this.contactRequestRepository.hasPendingRequest(user.getId(), user2.getId())).thenReturn(true);

    this.service.acceptContactRequest(user2.getEmail(), response);
  }

  @Test
  public void ShouldFailResponseContactRequestWhenRequestNotExists() throws ApiException {
    var response = new ResponseContactRequestDto();
    response.setRequesterCanonicalName("ds");
    response.setAcceptContact(false);

    Mockito
      .when(this.userRepository.findByEmail(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(PartialUserImpl.class)));
    Mockito
      .when(this.userRepository.findByCanonicalName(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(PartialUserImpl.class)));
    Mockito.when(this.contactRequestRepository.hasPendingRequest(anyString(), anyString())).thenReturn(false);

    assertThrows(Exception.class, () -> this.service.acceptContactRequest("email", response));
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void findByPartialName() {
    var partialName = ezRandom.nextObject(String.class);
    var countryName = ezRandom.nextObject(String.class);
    var skillName = ezRandom.nextObject(String.class);
    var orderBy = ezRandom.nextObject(SortUsersBy.class);
    var usersList = new ArrayList<User>();
    usersList.add(new User());
    var size = 20;
    var page = 0;

    Mockito
      .when(this.userRepository.findAllWithFilters(partialName, countryName, skillName, orderBy.label, page, size))
      .thenReturn(usersList);
    Mockito.when(this.userRepository.countWithFilters(partialName, countryName, skillName)).thenReturn(1);

    var result = this.service.findByPartialName("", page, size, partialName, countryName, skillName, orderBy);
    assertNotNull(result);
  }

  @Test
  void ShouldDeleteFavoritePost() {
    this.service.removeFavoritePost("email", "id");
  }

  @Test
  public void ShouldBlockUser() throws ApiException {
    var blocked = new UpdateUserBlockedStatusDto();
    blocked.setBlocked(true);

    Mockito
      .when(this.userRepository.findByCanonicalName(anyString()))
      .thenReturn(Optional.of(ezRandom.nextObject(PartialUserImpl.class)));

    this.service.setUserBlockedStatus("cname", blocked);
  }

  @Test
  public void ShouldSetUserToDelete() throws ApiException {
    var res = this.service.setEliminationDate("email");
    assertNotNull(res);
  }

  @Test
  public void ShouldDeleteContact() throws ApiException {
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var user2 = ezRandom.nextObject(PartialUserImpl.class);
    var response = new ResponseContactRequestDto();
    response.setRequesterCanonicalName(user.getCanonicalName());
    response.setAcceptContact(false);

    Mockito.when(this.userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));
    Mockito.when(this.userRepository.findByCanonicalName(user2.getCanonicalName())).thenReturn(Optional.of(user2));

    this.service.removeContact(user.getCanonicalName(), user2.getCanonicalName());
  }

  @Test
  public void ShouldCancelUserToDelete() throws ApiException {
    this.service.cancelEliminationDate("email");
  }

  @Test
  public void ShouldUpdateUserAndDeleteGitProfile() throws ApiException {
    var platform = ezRandom.nextObject(GitPlatform.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    user.setGitProfile(ezRandom.nextObject(PartialGitProfileImpl.class));
    var userDto = ezRandom.nextObject(UpdateUserDto.class);
    userDto.setGitProfile(null);
    var country = ezRandom.nextObject(Country.class);

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.countryRepository.findById(anyString())).thenReturn(Optional.of(country));

    var res = this.service.updateProfile(user.getCanonicalName(), userDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldUpdateUserAndUpdateGitPlatform() throws ApiException {
    var platform = ezRandom.nextObject(GitPlatform.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    user.setGitProfile(ezRandom.nextObject(PartialGitProfileImpl.class));

    var userDto = ezRandom.nextObject(UpdateUserDto.class);
    var country = ezRandom.nextObject(Country.class);

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.countryRepository.findById(anyString())).thenReturn(Optional.of(country));
    Mockito.when(this.gitPlatformRepository.findById(anyString())).thenReturn(Optional.of(platform));

    var res = this.service.updateProfile(user.getCanonicalName(), userDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldUpdateUserAndAddGitProfile() throws ApiException {
    var platform = ezRandom.nextObject(GitPlatform.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    user.setGitProfile(null);
    var userDto = ezRandom.nextObject(UpdateUserDto.class);
    var country = ezRandom.nextObject(Country.class);

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.countryRepository.findById(anyString())).thenReturn(Optional.of(country));
    Mockito.when(this.gitPlatformRepository.findById(anyString())).thenReturn(Optional.of(platform));

    var res = this.service.updateProfile(user.getCanonicalName(), userDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldUpdateUserAndUpdateGitUser() throws ApiException {
    var platform = ezRandom.nextObject(GitPlatform.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var userDto = ezRandom.nextObject(UpdateUserDto.class);

    userDto.setGitProfile(
      GitProfileDto
        .builder()
        .userName(ezRandom.nextObject(String.class))
        .platform(tech.onlycoders.backend.dto.user.GitPlatform.GITHUB)
        .build()
    );

    platform.setId(tech.onlycoders.backend.dto.user.GitPlatform.GITHUB.toString());
    user.setGitProfile(ezRandom.nextObject(PartialGitProfileImpl.class));
    var country = ezRandom.nextObject(Country.class);

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.countryRepository.findById(anyString())).thenReturn(Optional.of(country));

    var res = this.service.updateProfile(user.getCanonicalName(), userDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldGetUserLanguage() {
    Mockito
      .when(this.languageRepository.getUserLanguage(anyString()))
      .thenReturn(Language.builder().name("English").code("en").build());

    var res = this.service.getUserLanguage("canonical");

    assertNotNull(res);
  }

  @Test
  public void ShouldSetUserLanguage() throws ApiException {
    var dto = new UpdateUserLanguageDto();
    dto.setCode("en");

    Mockito
      .when(this.languageRepository.findById(anyString()))
      .thenReturn(Optional.of(Language.builder().name("English").code("en").build()));

    this.service.setUserLanguage("canonical", dto);
  }

  @Test
  public void ShouldFailSetUserLanguageWhenLangNotFound() {
    var dto = new UpdateUserLanguageDto();
    dto.setCode("en");

    Mockito.when(this.languageRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(Exception.class, () -> this.service.setUserLanguage("canonical", dto));
  }
}
