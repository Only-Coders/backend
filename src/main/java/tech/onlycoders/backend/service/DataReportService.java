package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.datareport.AttributeValueDto;
import tech.onlycoders.backend.dto.datareport.UsersQuantityReportDto;
import tech.onlycoders.backend.repository.DataReportRepository;
import tech.onlycoders.backend.repository.LanguageRepository;

@Service
@Transactional
public class DataReportService {

  private final DataReportRepository dataReportRepository;

  private final LanguageRepository languageRepository;

  private final Neo4jClient neo4jClient;

  public DataReportService(
    DataReportRepository dataReportRepository,
    LanguageRepository languageRepository,
    Neo4jClient neo4jClient
  ) {
    this.dataReportRepository = dataReportRepository;
    this.languageRepository = languageRepository;
    this.neo4jClient = neo4jClient;
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
    var query =
      "MATCH (p:Post)\n" +
      "WHERE p.createdAt > 1623713625000\n" +
      "WITH apoc.date.format(p.createdAt, \"ms\", \"yyyy-MM-dd\") AS date,\n" +
      "COUNT(p) as found\n" +
      "RETURN date, found";
    var resultList = neo4jClient.query(query).fetch().all();

    var result = new ArrayList<AttributeValueDto>();
    resultList
      .stream()
      .forEach(
        row -> {
          result.add(
            AttributeValueDto.builder().attribute((String) row.get("date")).value((Long) row.get("found")).build()
          );
        }
      );
    return result;
  }
}
