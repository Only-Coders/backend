package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.institute.response.ReadDegreeDto;
import tech.onlycoders.backend.dto.institute.response.ReadInstituteDto;
import tech.onlycoders.backend.dto.user.request.EducationExperienceDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.InstituteMapper;
import tech.onlycoders.backend.model.Degree;
import tech.onlycoders.backend.model.Institute;
import tech.onlycoders.backend.repository.DegreeRepository;
import tech.onlycoders.backend.repository.InstituteRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
@Transactional
public class InstituteService {

  private final InstituteRepository instituteRepository;
  private final InstituteMapper instituteMapper;
  private final DegreeRepository degreeRepository;
  private final UserRepository userRepository;

  public InstituteService(
    InstituteRepository instituteRepository,
    InstituteMapper instituteMapper,
    DegreeRepository degreeRepository,
    UserRepository userRepository
  ) {
    this.instituteRepository = instituteRepository;
    this.instituteMapper = instituteMapper;
    this.degreeRepository = degreeRepository;
    this.userRepository = userRepository;
  }

  public PaginateDto<ReadInstituteDto> listInstitutes(String instituteName, Integer page, Integer size) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedInstitutes = this.instituteRepository.findByNameContainingIgnoreCase(instituteName, pageRequest);
    var instituteDtoList = instituteMapper.listInstitutesToListReadInstituteDto(paginatedInstitutes.getContent());
    var pagination = new PaginateDto<ReadInstituteDto>();
    pagination.setContent(instituteDtoList);
    pagination.setCurrentPage(paginatedInstitutes.getNumber());
    pagination.setTotalPages(paginatedInstitutes.getTotalPages());
    pagination.setTotalElements(paginatedInstitutes.getNumberOfElements());
    return pagination;
  }

  public ReadInstituteDto createInstitute(CreateInstituteDto createInstituteDto) {
    var institute = new Institute();
    institute.setName(createInstituteDto.getName());
    this.instituteRepository.save(institute);
    return this.instituteMapper.instituteToReadInstituteDto(institute);
  }

  public ReadDegreeDto addUserDegree(String email, EducationExperienceDto educationExperienceDto) throws ApiException {
    var institute =
      this.instituteRepository.findById(educationExperienceDto.getId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.workplace-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    var degree = new Degree();
    degree.setInstitute(institute);
    degree.setSince(educationExperienceDto.getSince());
    degree.setUntil(educationExperienceDto.getUntil());
    degree.setDegree(educationExperienceDto.getDegree());
    degreeRepository.save(degree);
    degreeRepository.storeDegree(degree.getId(), user.getId());
    return instituteMapper.degreeToReadDegreeDto(degree);
  }

  public PaginateDto<ReadDegreeDto> getUserDegrees(String canonicalName, Integer page, Integer size) {
    var degreeList = degreeRepository.getUserDegrees(canonicalName, page * size, size);
    var countUserDegrees = degreeRepository.countUserDegrees(canonicalName);
    var workPositionDtoList = instituteMapper.degreesToReadDegreeDtos(degreeList);

    var amountPages = PaginationUtils.getPagesQuantity(countUserDegrees, size);
    var pagination = new PaginateDto<ReadDegreeDto>();
    pagination.setContent(workPositionDtoList);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(amountPages);
    pagination.setTotalElements(countUserDegrees);
    return pagination;
  }

  public void removeDegree(String email, String degreeId) throws ApiException {
    if (!degreeRepository.isOwner(email, degreeId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "error.user-not-owner");
    }
    degreeRepository.remove(degreeId);
  }
}
