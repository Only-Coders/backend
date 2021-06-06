package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.institute.response.ReadDegreeDto;
import tech.onlycoders.backend.dto.institute.response.ReadInstituteDto;
import tech.onlycoders.backend.model.Degree;
import tech.onlycoders.backend.model.Institute;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface InstituteMapper {
  List<ReadInstituteDto> listInstitutesToListReadInstituteDto(List<Institute> institutes);
  ReadInstituteDto instituteToReadInstituteDto(Institute organization);
  List<ReadDegreeDto> degreesToReadDegreeDtos(List<Degree> degree);
  ReadDegreeDto degreeToReadDegreeDto(Degree degree);
}
