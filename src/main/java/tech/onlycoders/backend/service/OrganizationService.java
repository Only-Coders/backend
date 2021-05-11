package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.organization.response.ReadOrganizationDto;
import tech.onlycoders.backend.mapper.OrganizationMapper;
import tech.onlycoders.backend.repository.OrganizationRepository;

@Service
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final OrganizationMapper organizationMapper;

  public OrganizationService(OrganizationRepository organizationRepository, OrganizationMapper organizationMapper) {
    this.organizationRepository = organizationRepository;
    this.organizationMapper = organizationMapper;
  }

  public PaginateDto<ReadOrganizationDto> listOrganizations(String organizationName, Integer page, Integer size) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedOrganizations =
      this.organizationRepository.findByNameContainingIgnoreCase(organizationName, pageRequest);
    var organizations = organizationMapper.listOrganizationsToListReadOrganizationDto(
      paginatedOrganizations.getContent()
    );
    var pagination = new PaginateDto<ReadOrganizationDto>();
    pagination.setContent(organizations);
    pagination.setCurrentPage(paginatedOrganizations.getNumber());
    pagination.setTotalPages(paginatedOrganizations.getTotalPages());
    pagination.setTotalElements(paginatedOrganizations.getNumberOfElements());
    return pagination;
  }
}
