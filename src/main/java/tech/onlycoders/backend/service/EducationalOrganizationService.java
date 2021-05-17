package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.workplace.request.CreateEducationalOrganizationDto;
import tech.onlycoders.backend.dto.workplace.response.ReadEducationalOrganizationDto;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.model.EducationalOrganization;
import tech.onlycoders.backend.repository.EducationalOrganizationRepository;

@Service
public class EducationalOrganizationService {

  private final EducationalOrganizationRepository organizationRepository;
  private final WorkplaceMapper workplaceMapper;

  public EducationalOrganizationService(
    EducationalOrganizationRepository organizationRepository,
    WorkplaceMapper workplaceMapper
  ) {
    this.organizationRepository = organizationRepository;
    this.workplaceMapper = workplaceMapper;
  }

  public PaginateDto<ReadEducationalOrganizationDto> listEducationalOrganizations(
    String organizationName,
    Integer page,
    Integer size
  ) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedOrganizations =
      this.organizationRepository.findByNameContainingIgnoreCase(organizationName, pageRequest);
    var organizations = workplaceMapper.listEducationalOrganizationsToListReadEducationalOrganizationDto(
      paginatedOrganizations.getContent()
    );
    var pagination = new PaginateDto<ReadEducationalOrganizationDto>();
    pagination.setContent(organizations);
    pagination.setCurrentPage(paginatedOrganizations.getNumber());
    pagination.setTotalPages(paginatedOrganizations.getTotalPages());
    pagination.setTotalElements(paginatedOrganizations.getNumberOfElements());
    return pagination;
  }

  public ReadEducationalOrganizationDto createEducationalOrganization(
    CreateEducationalOrganizationDto createOrganizationDto
  ) {
    var organization = new EducationalOrganization();
    organization.setName(createOrganizationDto.getName());
    this.organizationRepository.save(organization);
    return this.workplaceMapper.educationalOrganizationsReadToEducationalOrganizationDto(organization);
  }
}
