package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.repository.WorkPositionRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @InjectMocks
  private AuthService service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FirebaseService firebaseService;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private WorkPositionRepository workPositionRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(jwtService, "SECRET_KEY", "asdasd");
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", 5000);
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", 5000);
    ReflectionTestUtils.setField(service, "jwtService", jwtService);
  }

  @Test
  public void ShouldFailToLoginWithNotVerifiedEmail() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenThrow(ApiException.class);
    assertThrows(ApiException.class, () -> this.service.authenticate(authRequest));
  }

  @Test
  public void ShouldReturnAnAccessTokenWhenEmailIsVerified() throws ApiException {
    var person = ezRandom.nextObject(User.class);
    person.setRole(Role.builder().name("USER").build());
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.userRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn(person.getEmail());
    var token = this.service.authenticate(authRequest);
    assertNotNull(token);
  }

  @Test
  public void ShouldReturnAnAccessTokenAfterCreatedUser() {
    var user = ezRandom.nextObject(User.class);
    var token = this.service.postCreateUser(user);
    assertNotNull(token);
  }

  @Test
  public void ShouldReturnAnAccessTokenWhenEmailIsVerifiedAndUserIsNotRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var token = this.service.authenticate(authRequest);
    assertNotNull(token);
  }

  @Test
  public void ShouldRefreshTokenWhenUserHasNotBeenRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var authResponseDto = this.service.authenticate(authRequest);
    this.service.refreshToken(authResponseDto.getToken());
  }

  @Test
  public void ShouldRefreshTokenWhenUserHasBeenRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    var person = ezRandom.nextObject(User.class);
    var workPosition = ezRandom.nextObject(WorkPosition.class);
    person.setRole(Role.builder().name("USER").build());
    person.setSecurityUpdate(null);
    Mockito.when(this.userRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
    Mockito.when(this.workPositionRepository.getUserCurrentPosition(anyString())).thenReturn(Optional.of(workPosition));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn(person.getEmail());
    var authResponseDto = this.service.authenticate(authRequest);
    this.service.refreshToken(authResponseDto.getToken());
  }

  @Test
  public void ShouldRefreshTokenWhenAdminHasBeenRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    var person = ezRandom.nextObject(Admin.class);
    person.setEmail("admin@onlycoders.tech");
    person.setRole(Role.builder().name("ADMIN").build());
    person.setSecurityUpdate(null);
    Mockito.when(this.adminRepository.findByEmail(person.getEmail())).thenReturn(Optional.of(person));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn(person.getEmail());
    var authResponseDto = this.service.authenticate(authRequest);
    this.service.refreshToken(authResponseDto.getToken());
  }
}
