package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.model.Workplace;
import tech.onlycoders.backend.repository.WorkplaceRepository;

@Service
public class WorkplaceService {

  private final WorkplaceRepository workplaceRepository;
  private final WorkplaceMapper workplaceMapper;

  public WorkplaceService(WorkplaceRepository workplaceRepository, WorkplaceMapper workplaceMapper) {
    this.workplaceRepository = workplaceRepository;
    this.workplaceMapper = workplaceMapper;
  }

  public PaginateDto<ReadWorkplaceDto> listWorkplaces(String workplaceName, Integer page, Integer size) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedWorkplaces = this.workplaceRepository.findByNameContainingIgnoreCase(workplaceName, pageRequest);
    var workplaces = workplaceMapper.listWorkplacesToListReadWorkplaceDto(paginatedWorkplaces.getContent());
    var pagination = new PaginateDto<ReadWorkplaceDto>();
    pagination.setContent(workplaces);
    pagination.setCurrentPage(paginatedWorkplaces.getNumber());
    pagination.setTotalPages(paginatedWorkplaces.getTotalPages());
    pagination.setTotalElements(paginatedWorkplaces.getNumberOfElements());
    return pagination;
  }

  public ReadWorkplaceDto createWorkplace(CreateWorkplaceDto createWorkplaceDto) {
    var workplace = Workplace.builder().name(createWorkplaceDto.getName()).build();
    this.workplaceRepository.save(workplace);
    return this.workplaceMapper.workplaceToReadWorkplaceDto(workplace);
  }
}
