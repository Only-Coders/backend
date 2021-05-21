package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.contactrequest.request.CreateContactRequestDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.*;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;

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
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
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
    var user1 = ezRandom.nextObject(User.class);
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
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.of(user2));

    this.service.sendContactRequest(email, reqDto);
  }

  @Test
  public void ShouldFailWithConflictWhenSendContactRequest() {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
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
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var reqDto = ezRandom.nextObject(CreateContactRequestDto.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(reqDto.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.sendContactRequest(email, reqDto));
  }

  @Test
  public void ShouldAddFavoritePost() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var post = new Post();
    var email = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.postRepository.findById(postId)).thenReturn(Optional.of(post));

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
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.postRepository.findById(postId)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addFavoritePost(email, postId));
  }

  @Test
  public void ShouldReturnFavoritePosts() throws ApiException {
    var email = ezRandom.nextObject(String.class);
    var postList = new ArrayList<Post>();
    postList.add(new Post());
    var size = 20;
    var page = 0;

    Mockito.when(this.postRepository.getUserFavoritePostTotalQuantity(email)).thenReturn(1);
    Mockito.when(this.postRepository.getUserFavoritePosts(email, page, size)).thenReturn(postList);

    var result = this.service.getFavoritePosts(email, page, size);
    assertNotNull(result);
  }

  @Test
  public void ShouldFailReturnFavoritePostsWhenUserNotFound() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var size = 20;
    var page = 0;

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.postRepository.getUserFavoritePostTotalQuantity(email)).thenReturn(0);

    var result = this.service.getFavoritePosts(email, page, size);
    assertNotNull(result);
  }

  @Test
  public void ShouldUnfollowUser() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);
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
    var user1 = ezRandom.nextObject(User.class);
    var email = ezRandom.nextObject(String.class);
    var cName = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
    Mockito.when(this.userRepository.findByCanonicalName(cName)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.unfollowUser(email, cName));
  }

  @Test
  public void ShouldUnSendRequest() throws ApiException {
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);

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
    var user1 = ezRandom.nextObject(User.class);
    var user2 = ezRandom.nextObject(User.class);

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

    Mockito.when(this.contactRequestRepository.getReceivedContactResquestTotalQuantity(anyString())).thenReturn(1);
    Mockito
      .when(this.contactRequestRepository.getReceivedContactResquests(anyString(), anyInt(), anyInt()))
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

    Mockito.when(this.contactRequestRepository.getReceivedContactResquestTotalQuantity(anyString())).thenReturn(0);
    Mockito.when(this.userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.getReceivedContactRequests(email, 0, 10));
  }

  @Test
  public void ShouldGetContactsOfUser() {
    var canonicalName = ezRandom.nextObject(String.class);
    var usersList = new ArrayList<User>();
    usersList.add(new User());
    var size = 20;
    var page = 0;

    Mockito.when(this.userRepository.getContacts(canonicalName)).thenReturn(usersList);
    Mockito.when(this.userRepository.countContacts(canonicalName)).thenReturn(1);

    var result = this.service.getMyContacts(canonicalName, page, size);
    assertNotNull(result);
  }
}
