package tech.onlycoders.backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.UserMapper;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @InjectMocks
  private UserService service;

  @Mock
  private UserRepository userRepository;

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
}
