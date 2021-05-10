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
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.service.AdminService;

@RunWith(MockitoJUnitRunner.class)
public class AdminServiceTest {

  @InjectMocks
  private AdminService service;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private FirebaseService firebaseService;

  private final EasyRandom ezRandom = new EasyRandom();

  @Before
  public void setUp() {
    var adminMapper = Mappers.getMapper(AdminMapper.class);
    ReflectionTestUtils.setField(service, "adminMapper", adminMapper);
  }

  @Test
  public void ShouldFailWhenFirebaseReturnsException() throws ApiException {
    var createAdminDto = ezRandom.nextObject(CreateAdminDto.class);
    Mockito.doThrow(ApiException.class).when(this.firebaseService).createUser(anyString());
    assertThrows(ApiException.class, () -> this.service.createAdmin(createAdminDto));
  }

  @Test
  public void ShouldFailWhenUserAlreadyExists() {
    var createAdminDto = ezRandom.nextObject(CreateAdminDto.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.of(new Admin()));
    assertThrows(ApiException.class, () -> this.service.createAdmin(createAdminDto));
  }

  @Test
  public void ShouldCreateAdmin() throws ApiException {
    var createAdminDto = ezRandom.nextObject(CreateAdminDto.class);
    Mockito.when(this.personRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    var result = this.service.createAdmin(createAdminDto);
    assertEquals(result.getEmail(), createAdminDto.getEmail());
    assertEquals(result.getFirstName(), createAdminDto.getFirstName());
    assertEquals(result.getLastName(), createAdminDto.getLastName());
  }
}
