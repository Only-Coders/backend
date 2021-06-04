package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.model.Workplace;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.repository.WorkPositionRepository;
import tech.onlycoders.backend.repository.WorkplaceRepository;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
@Transactional
public class WorkplaceService {

  private final WorkplaceRepository workplaceRepository;
  private final WorkplaceMapper workplaceMapper;
  private final UserRepository userRepository;
  private final WorkPositionRepository workPositionRepository;
  private final WorkPositionMapper workPositionMapper;

  public WorkplaceService(
    WorkplaceRepository workplaceRepository,
    WorkplaceMapper workplaceMapper,
    UserRepository userRepository,
    WorkPositionRepository workPositionRepository,
    WorkPositionMapper workPositionMapper
  ) {
    this.workplaceRepository = workplaceRepository;
    this.workplaceMapper = workplaceMapper;
    this.userRepository = userRepository;
    this.workPositionRepository = workPositionRepository;
    this.workPositionMapper = workPositionMapper;
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

  public ReadWorkPositionDto addWorkExperience(String email, WorkExperienceDto workExperienceDto) throws ApiException {
    var workplace =
      this.workplaceRepository.findById(workExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.workplace-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var workPosition = new WorkPosition();
    workPosition.setWorkplace(workplace);
    workPosition.setSince(workExperienceDto.getSince());
    workPosition.setUntil(workExperienceDto.getUntil());
    workPosition.setPosition(workExperienceDto.getPosition());
    this.workPositionRepository.save(workPosition);
    this.workPositionRepository.addUserWorkPosition(workPosition.getId(), user.getId());
    return this.workPositionMapper.workPositionToReadWorkPositionDto(workPosition);
  }

  public PaginateDto<ReadWorkPositionDto> getUserJobs(String canonicalName, Integer page, Integer size) {
    var workPositions = workPositionRepository.getUserJobs(canonicalName, page * size, size);
    var countUserJobs = workPositionRepository.countUserJobs(canonicalName);
    var workPositionDtoList = workPositionMapper.workPositionsToReadWorkPositionDtos(workPositions);

    var amountPages = PaginationUtils.getPagesQuantity(countUserJobs, size);
    var pagination = new PaginateDto<ReadWorkPositionDto>();
    pagination.setContent(workPositionDtoList);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(amountPages);
    pagination.setTotalElements(countUserJobs);
    return pagination;
  }
}
