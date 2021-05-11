package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.organization.response.ReadEducationalOrganizationDto;
import tech.onlycoders.backend.dto.organization.response.ReadOrganizationDto;
import tech.onlycoders.backend.model.EducationalOrganization;
import tech.onlycoders.backend.model.Organization;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrganizationMapper {
  List<ReadOrganizationDto> listOrganizationsToListReadOrganizationDto(List<Organization> organizations);
  ReadOrganizationDto organizationsReadOrganizationDto(Organization organization);

  List<ReadEducationalOrganizationDto> listEducationalOrganizationsToListReadEducationalOrganizationDto(
    List<EducationalOrganization> organizations
  );
  ReadEducationalOrganizationDto educationalOrganizationsReadToEducationalOrganizationDto(
    EducationalOrganization organization
  );
}
