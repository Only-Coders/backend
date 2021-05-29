package tech.onlycoders.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.dto.reporttype.response.ReadReportTypeDto;
import tech.onlycoders.backend.mapper.CountryMapper;
import tech.onlycoders.backend.mapper.ReportTypeMapper;
import tech.onlycoders.backend.repository.CountryRepository;
import tech.onlycoders.backend.repository.ReportTypeRepository;

@Service
@Transactional
public class ReportTypeService {

  private final ReportTypeRepository repository;
  private final ReportTypeMapper mapper;

  public ReportTypeService(ReportTypeRepository reportTypeRepository, ReportTypeMapper reportTypeMapper) {
    this.repository = reportTypeRepository;
    this.mapper = reportTypeMapper;
  }

  public List<ReadReportTypeDto> getTypesByLanguage(String language) {
    var result = this.repository.findAllByLanguage(language);
    return this.mapper.listReportTypeToListReadReportTypeDto(result);
  }
}
