package tech.onlycoders.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.datareport.UsersQuantityReportDto;
import tech.onlycoders.backend.repository.DataReportRepository;

@Service
@Transactional
public class DataReportService {

  private final DataReportRepository repo;

  public DataReportService(DataReportRepository dataReportRepository) {
    this.repo = dataReportRepository;
  }

  public UsersQuantityReportDto getUsersQuantity() {
    return UsersQuantityReportDto
      .builder()
      .totalBannedUsers(repo.getBlacklistedUsersQuantity())
      .totalBlockedUsers(repo.getBlockedUsersQuantity())
      .totalActiveUsers(repo.getUsersQuantity())
      .build();
  }
}
