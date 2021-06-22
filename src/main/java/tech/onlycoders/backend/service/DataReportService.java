package tech.onlycoders.backend.service;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.datareport.AttributeValueDto;
import tech.onlycoders.backend.dto.datareport.PostAndReactionsPerHourDto;
import tech.onlycoders.backend.dto.datareport.UsersQuantityReportDto;
import tech.onlycoders.backend.repository.DataReportRepository;
import tech.onlycoders.backend.repository.GenericRepository;
import tech.onlycoders.backend.repository.LanguageRepository;

@Service
@Transactional
public class DataReportService {

  private final DataReportRepository dataReportRepository;

  private final LanguageRepository languageRepository;

  private final GenericRepository genericRepository;

  public DataReportService(
    DataReportRepository dataReportRepository,
    LanguageRepository languageRepository,
    GenericRepository genericRepository
  ) {
    this.dataReportRepository = dataReportRepository;
    this.languageRepository = languageRepository;
    this.genericRepository = genericRepository;
  }

  public UsersQuantityReportDto getUsersQuantity() {
    return UsersQuantityReportDto
      .builder()
      .totalBannedUsers(dataReportRepository.getBlacklistedUsersQuantity())
      .totalBlockedUsers(dataReportRepository.getBlockedUsersQuantity())
      .totalActiveUsers(dataReportRepository.getUsersQuantity())
      .build();
  }

  public List<AttributeValueDto> getLanguageUse() {
    var langList = languageRepository.findAll();
    var report = new ArrayList<AttributeValueDto>();

    for (var lang : langList) {
      report.add(
        AttributeValueDto
          .builder()
          .attribute(lang.getName())
          .value(languageRepository.getLanguageUseQuantity(lang.getCode()))
          .build()
      );
    }

    return report;
  }

  public List<AttributeValueDto> getPostsPerDay() {
    var resultList = genericRepository.getPostsPerDay();
    var result = new ArrayList<AttributeValueDto>();

    resultList.forEach(
      row -> {
        result.add(
          AttributeValueDto.builder().attribute((String) row.get("date")).value((Long) row.get("found")).build()
        );
      }
    );
    return result;
  }

  public List<PostAndReactionsPerHourDto> getPostsAndReactionsPerHour() {
    var resultMap = new HashMap<String, PostAndReactionsPerHourDto>();
    for (int i = 0; i < 24; i++) {
      var key = (i < 10 ? "0" + i : i) + ":00";
      resultMap.put(key, PostAndReactionsPerHourDto.builder().hour(key).posts(0L).reactions(0L).build());
    }
    var postsList = genericRepository.getPostsPerHour();
    postsList.forEach(
      row -> {
        var item = resultMap.get((String) row.get("hour"));
        item.setPosts((Long) row.get("found"));
      }
    );

    var reactionsList = genericRepository.getReactionsPerHour();
    reactionsList.forEach(
      row -> {
        var item = resultMap.get((String) row.get("hour"));
        item.setReactions((Long) row.get("found"));
      }
    );

    var result = new ArrayList<PostAndReactionsPerHourDto>();
    resultMap
      .entrySet()
      .stream()
      .sorted(Map.Entry.<String, PostAndReactionsPerHourDto>comparingByKey())
      .forEach(
        element -> {
          result.add(element.getValue());
        }
      );

    return result;
  }
}
