package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Date;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.joda.time.DateTime;
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
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private JwtService jwtService;

  @InjectMocks
  private AuthService service;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private FirebaseService firebaseService;

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
    var person = new User();
    person.setRole(Role.builder().name("USER").build());
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
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
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var token = this.service.authenticate(authRequest);
    assertNotNull(token);
  }

  @Test
  public void ShouldRefreshTokenWhenUserHasNotBeenRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var authResponseDto = this.service.authenticate(authRequest);
    this.service.refreshToken(authResponseDto.getToken());
  }

  @Test
  public void ShouldRefreshTokenWhenUserHasBeenRegistered() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    var person = new User();
    person.setRole(Role.builder().name("USER").build());
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var authResponseDto = this.service.authenticate(authRequest);
    this.service.refreshToken(authResponseDto.getToken());
  }

  @Test
  public void ShouldFailToRefreshTokenWhenUserHasDoneASecurityLogout() throws ApiException {
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);

    Date dt = new Date();
    DateTime dtOrg = new DateTime(dt);
    DateTime dtPlusOne = dtOrg.plusDays(1);

    var person = new User();
    person.setRole(Role.builder().name("USER").build());
    person.setSecurityUpdate(dtPlusOne.toDate());

    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(person));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var authResponseDto = this.service.authenticate(authRequest);
    assertThrows(ApiException.class, () -> this.service.refreshToken(authResponseDto.getToken()));
  }
}
