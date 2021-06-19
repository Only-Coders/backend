package tech.onlycoders.backend.service;

import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import tech.onlycoders.backend.dto.recentsearch.request.CreateRecentSearchDto;
import tech.onlycoders.backend.model.RecentSearch;

@ExtendWith(MockitoExtension.class)
public class RecentSearchServiceTest {

  @InjectMocks
  private RecentSearchService service;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private static RedissonClient client;

  private final String canonicalName = ezRandom.nextObject(String.class);

  @BeforeEach
  private void beforeEach() {
    client.getSet(this.canonicalName).clear();
  }

  @BeforeAll
  private static void beforeAll() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");
    client = Redisson.create(config);
  }

  @Test
  public void ShouldAddNewRecentSearch() {
    var set = ezRandom.objects(RecentSearch.class, 10).collect(Collectors.toSet());
    var recentSearchDto = ezRandom.nextObject(CreateRecentSearchDto.class);
    var redisSet = client.getSet(canonicalName);
    redisSet.addAll(set);
    service.addRecentSearch(canonicalName, recentSearchDto);
  }

  @Test
  public void ShouldUpdateARecentSearch() {
    var set = ezRandom.objects(RecentSearch.class, 10).collect(Collectors.toSet());
    var recentSearchDto = ezRandom.nextObject(CreateRecentSearchDto.class);
    var redisSet = client.getSet(canonicalName);
    redisSet.addAll(set);
    service.addRecentSearch(canonicalName, recentSearchDto);
    service.addRecentSearch(canonicalName, recentSearchDto);
  }

  @Test
  public void ShouldReadRecentSearch() {
    var set = ezRandom.objects(RecentSearch.class, 10).collect(Collectors.toSet());
    var redisSet = client.getSet(canonicalName);
    redisSet.addAll(set);
    service.readAllRecentSearch(canonicalName);
  }

  @Test
  public void ShouldClearRecentSearch() {
    var set = ezRandom.objects(RecentSearch.class, 10).collect(Collectors.toSet());
    var redisSet = client.getSet(canonicalName);
    redisSet.addAll(set);
    service.clearAllRecentSearch(canonicalName);
    var result = service.readAllRecentSearch(canonicalName);
    Assertions.assertEquals(0, result.size());
  }
}
