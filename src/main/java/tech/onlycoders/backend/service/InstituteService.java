package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.institute.response.ReadInstituteDto;
import tech.onlycoders.backend.mapper.InstituteMapper;
import tech.onlycoders.backend.model.Institute;
import tech.onlycoders.backend.repository.InstituteRepository;

@Service
public class InstituteService {

  private final InstituteRepository instituteRepository;
  private final InstituteMapper instituteMapper;

  public InstituteService(InstituteRepository instituteRepository, InstituteMapper instituteMapper) {
    this.instituteRepository = instituteRepository;
    this.instituteMapper = instituteMapper;
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
}
