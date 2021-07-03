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
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.BlackListMapper;
import tech.onlycoders.backend.model.BlackList;
import tech.onlycoders.backend.repository.BlacklistRepository;

@ExtendWith(MockitoExtension.class)
public class BlackListServiceTest {

  @InjectMocks
  private BlackListService service;

  @Mock
  private BlacklistRepository blacklistRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final BlackListMapper blackListMapper = Mappers.getMapper(BlackListMapper.class);

  @Test
  public void ShouldPaginateAdmins() {
    var users = ezRandom.objects(BlackList.class, 10).collect(Collectors.toList());
    Mockito
      .when(this.blacklistRepository.paginateAllBlackListedUsers(anyString(), anyInt(), anyInt()))
      .thenReturn(users);
    Mockito.when(this.blacklistRepository.countAllBlackListedUsers(anyString())).thenReturn(users.size());
    var result = this.service.paginateBlackList(ezRandom.nextObject(String.class), 1, 1);
    assertEquals(10, result.getTotalElements());
    assertEquals(10, result.getContent().size());
  }

  @Test
  public void ShouldDelete() {
    this.service.removeUser("asd");
  }

  @Test
  public void ShouldNotAddUser() {
    Mockito.when(this.blacklistRepository.findById("asd")).thenReturn(Optional.of(new BlackList()));
    assertThrows(ApiException.class, () -> this.service.addUser("asd"));
  }

  @Test
  public void ShouldAddUser() throws ApiException {
    Mockito.when(this.blacklistRepository.findById("asd")).thenReturn(Optional.empty());
    var result = this.service.addUser("asd");
    assertEquals(result.getEmail(), "asd");
  }
}
