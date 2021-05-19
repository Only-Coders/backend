package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.dto.skill.response.ReadSkillDto;
import tech.onlycoders.backend.mapper.SkillMapper;
import tech.onlycoders.backend.model.Skill;
import tech.onlycoders.backend.repository.SkillRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@Service
public class SkillService {

  private final SkillRepository skillRepository;

  private final SkillMapper skillMapper;

  public SkillService(SkillRepository skillRepository, SkillMapper skillMapper) {
    this.skillRepository = skillRepository;
    this.skillMapper = skillMapper;
  }

  public ReadSkillDto createSkill(CreateSkillDto createSkillDto) {
    var canonicalName = CanonicalFactory.getCanonicalName(createSkillDto.getName());
    var persistedSkill = skillRepository
      .findById(canonicalName)
      .orElseGet(
        () -> {
          var skill = Skill.builder().canonicalName(canonicalName).name(createSkillDto.getName()).build();
          skillRepository.save(skill);
          return skill;
        }
      );
    return this.skillMapper.skillToReadSkillDto(persistedSkill);
  }

  public PaginateDto<ReadSkillDto> listSkills(String skillName, Integer page, Integer size) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedOrganizations = this.skillRepository.findByNameContainingIgnoreCase(skillName, pageRequest);
    var skills = skillMapper.listSkillsToListReadSkillDto(paginatedOrganizations.getContent());
    var pagination = new PaginateDto<ReadSkillDto>();
    pagination.setContent(skills);
    pagination.setCurrentPage(paginatedOrganizations.getNumber());
    pagination.setTotalPages(paginatedOrganizations.getTotalPages());
    pagination.setTotalElements(paginatedOrganizations.getNumberOfElements());
    return pagination;
  }
}
