package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.neo4j.core.Neo4jClient;
import tech.onlycoders.backend.model.Language;
import tech.onlycoders.backend.repository.DataReportRepository;
import tech.onlycoders.backend.repository.GenericRepository;
import tech.onlycoders.backend.repository.LanguageRepository;

@ExtendWith(MockitoExtension.class)
public class DataReportServiceTest {

  @InjectMocks
  private DataReportService service;

  @Mock
  private DataReportRepository repoMock;

  @Mock
  private LanguageRepository languageRepository;

  @Mock
  private GenericRepository genericRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Test
  public void ShouldGetUsersQuantity() {
    Mockito.when(repoMock.getUsersQuantity()).thenReturn(1);
    Mockito.when(repoMock.getBlacklistedUsersQuantity()).thenReturn(1);
    Mockito.when(repoMock.getBlockedUsersQuantity()).thenReturn(1);
    var res = service.getUsersQuantity();

    assertNotNull(res);
  }

  @Test
  public void ShouldGetLanguageUse() {
    var list = new ArrayList<Language>();
    list.add(Language.builder().code("en").name("English").build());
    Mockito.when(this.languageRepository.findAll()).thenReturn(list);
    Mockito.when(this.languageRepository.getLanguageUseQuantity(anyString())).thenReturn(1L);
    var res = service.getLanguageUse();

    assertNotNull(res);
  }

  @Test
  public void ShouldGetPostsPerDay() {
    var list = new ArrayList<Map<String, Object>>();
    var map = new HashMap<String, Object>();
    map.put("date", "2021-06-01");
    map.put("found", 1L);
    list.add(map);
    Mockito.when(genericRepository.getPostsPerDay()).thenReturn(list);
    var res = service.getPostsPerDay();

    assertNotNull(res);
  }

  @Test
  public void ShouldGetPostsAndReactionsPerHour() {
    var list = new ArrayList<Map<String, Object>>();
    var map = new HashMap<String, Object>();
    map.put("hour", "22:00");
    map.put("found", 1L);
    list.add(map);
    Mockito.when(genericRepository.getPostsPerHour()).thenReturn(list);
    Mockito.when(genericRepository.getReactionsPerHour()).thenReturn(list);
    var res = service.getPostsAndReactionsPerHour();

    assertNotNull(res);
  }
}
