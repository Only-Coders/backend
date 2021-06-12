package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.List;
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

  public DataReportService(DataReportRepository dataReportRepository, LanguageRepository languageRepository) {
    this.dataReportRepository = dataReportRepository;
    this.languageRepository = languageRepository;
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
}
