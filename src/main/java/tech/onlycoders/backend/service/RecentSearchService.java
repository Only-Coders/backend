package tech.onlycoders.backend.service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.recentsearch.request.CreateRecentSearchDto;
import tech.onlycoders.backend.dto.recentsearch.response.ReadRecentSearchDto;
import tech.onlycoders.backend.model.RecentSearch;

@Service
@Transactional
public class RecentSearchService {

  private final RedissonClient client;

  public RecentSearchService(RedissonClient client) {
    this.client = client;
  }

  public void addRecentSearch(String canonicalName, CreateRecentSearchDto createPostDto) {
    RSet<RecentSearch> set = client.getSet(canonicalName);
    set
      .stream()
      .filter(recentSearch -> recentSearch.getCanonicalName().equalsIgnoreCase(createPostDto.getCanonicalName()))
      .findFirst()
      .ifPresentOrElse(
        recentSearch -> {
          set.remove(recentSearch);
          recentSearch.setCreatedAt(Instant.now().toEpochMilli());
          set.add(recentSearch);
        },
        () -> {
          var recentSearch = RecentSearch
            .builder()
            .canonicalName(createPostDto.getCanonicalName())
            .fullName(createPostDto.getFullName())
            .imageURI(createPostDto.getImageURI())
            .createdAt(Instant.now().toEpochMilli())
            .build();
          set.add(recentSearch);
        }
      );
  }

  public List<ReadRecentSearchDto> readAllRecentSearch(String canonicalName) {
    RSet<RecentSearch> set = client.getSet(canonicalName);
    var result = set
      .readAll()
      .stream()
      .map(
        recentSearch ->
          ReadRecentSearchDto
            .builder()
            .canonicalName(recentSearch.getCanonicalName())
            .fullName(recentSearch.getFullName())
            .imageURI(recentSearch.getImageURI())
            .createdAt(recentSearch.getCreatedAt())
            .build()
      )
      .collect(Collectors.toList());
    Collections.reverse(result);
    return result;
  }

  public void clearAllRecentSearch(String canonicalName) {
    RSet<RecentSearch> set = client.getSet(canonicalName);
    set.clear();
  }
}
