package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

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
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.RoleEnum;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

  @InjectMocks
  private AdminService service;

  @Mock
  private AdminRepository adminRepository;

  @Mock
  private PersonRepository personRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private FirebaseService firebaseService;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final AdminMapper adminMapper = Mappers.getMapper(AdminMapper.class);

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
    Mockito.when(this.roleRepository.findById(anyString())).thenReturn(Optional.of(new Role()));
    var result = this.service.createAdmin(createAdminDto);
    assertEquals(result.getEmail(), createAdminDto.getEmail());
    assertEquals(result.getFirstName(), createAdminDto.getFirstName());
    assertEquals(result.getLastName(), createAdminDto.getLastName());
  }

  @Test
  public void ShouldPaginatePeople() {
    var users = ezRandom.objects(Person.class, 10).collect(Collectors.toList());
    Mockito.when(this.personRepository.paginateAllPeople(anyString(), anyInt(), anyInt())).thenReturn(users);
    Mockito.when(this.personRepository.countAllPeople(anyString())).thenReturn(users.size());
    var result = this.service.paginateAllUsers(ezRandom.nextObject(String.class), null, 1, 1);
    assertEquals(10, result.getTotalElements());
    assertEquals(10, result.getContent().size());
  }

  @Test
  public void ShouldPaginateUsers() {
    var users = ezRandom.objects(Person.class, 10).collect(Collectors.toList());
    Mockito.when(this.personRepository.paginateAllUsers(anyString(), anyInt(), anyInt())).thenReturn(users);
    Mockito.when(this.personRepository.countAllUsers(anyString())).thenReturn(users.size());
    var result = this.service.paginateAllUsers(ezRandom.nextObject(String.class), RoleEnum.USER, 1, 1);
    assertEquals(10, result.getTotalElements());
    assertEquals(10, result.getContent().size());
  }

  @Test
  public void ShouldPaginateAdmins() {
    var users = ezRandom.objects(Person.class, 10).collect(Collectors.toList());
    Mockito.when(this.personRepository.paginateAllAdmins(anyString(), anyInt(), anyInt())).thenReturn(users);
    Mockito.when(this.personRepository.countAllAdmins(anyString())).thenReturn(users.size());
    var result = this.service.paginateAllUsers(ezRandom.nextObject(String.class), RoleEnum.ADMIN, 1, 1);
    assertEquals(10, result.getTotalElements());
    assertEquals(10, result.getContent().size());
  }
}
