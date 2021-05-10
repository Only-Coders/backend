package tech.onlycoders.backend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @InjectMocks
  private UserService service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PersonRepository personRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Before
  public void setUp() {
    var userMapper = Mappers.getMapper(UserMapper.class);
    ReflectionTestUtils.setField(service, "userMapper", userMapper);
  }

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
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    this.service.createUser(email, createUserDto);
  }

  @Test
  public void ShouldFailToCreateNewUser() throws ApiException {
    var createUserDto = ezRandom.nextObject(CreateUserDto.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    assertThrows(ApiException.class, () -> this.service.createUser(email, createUserDto));
  }
}
