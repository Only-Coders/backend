package tech.onlycoders.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.service.AuthService;
import tech.onlycoders.backend.service.JwtService;

@RunWith(MockitoJUnitRunner.class)
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

  @Before
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
    var authRequest = ezRandom.nextObject(AuthRequestDto.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(new Person()));
    Mockito.when(this.firebaseService.verifyFirebaseToken(anyString())).thenReturn("some@email.com");
    var token = this.service.authenticate(authRequest);
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
}
