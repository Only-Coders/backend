package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.reporttype.response.ReadReportTypeDto;
import tech.onlycoders.backend.model.ReportType;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ReportTypeMapper {
  List<ReadReportTypeDto> listReportTypeToListReadReportTypeDto(List<ReportType> result);
}
