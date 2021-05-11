package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.organization.request.CreateEducationalOrganizationDto;
import tech.onlycoders.backend.dto.organization.request.CreateOrganizationDto;
import tech.onlycoders.backend.dto.organization.response.ReadEducationalOrganizationDto;
import tech.onlycoders.backend.dto.organization.response.ReadOrganizationDto;
import tech.onlycoders.backend.mapper.OrganizationMapper;
import tech.onlycoders.backend.model.EducationalOrganization;
import tech.onlycoders.backend.model.Organization;
import tech.onlycoders.backend.repository.EducationalOrganizationRepository;
import tech.onlycoders.backend.repository.OrganizationRepository;

@Service
public class EducationalOrganizationService {

  private final EducationalOrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;

  public EducationalOrganizationService(
    EducationalOrganizationRepository organizationRepository,
    OrganizationMapper organizationMapper
  ) {
    this.organizationRepository = organizationRepository;
    this.organizationMapper = organizationMapper;
  }

  public PaginateDto<ReadEducationalOrganizationDto> listEducationalOrganizations(
    String organizationName,
    Integer page,
    Integer size
  ) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedOrganizations =
      this.organizationRepository.findByNameContainingIgnoreCase(organizationName, pageRequest);
    var organizations = organizationMapper.listEducationalOrganizationsToListReadEducationalOrganizationDto(
      paginatedOrganizations.getContent()
    );
    var pagination = new PaginateDto<ReadEducationalOrganizationDto>();
    pagination.setContent(organizations);
    pagination.setCurrentPage(paginatedOrganizations.getNumber());
    pagination.setTotalPages(paginatedOrganizations.getTotalPages());
    pagination.setTotalElements(paginatedOrganizations.getNumberOfElements());
    return pagination;
  }
}
