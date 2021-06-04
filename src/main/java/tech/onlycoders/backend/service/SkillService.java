package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.dto.skill.response.ReadSkillDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.SkillMapper;
import tech.onlycoders.backend.model.Skill;
import tech.onlycoders.backend.repository.SkillRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
@Transactional
public class SkillService {

  private final SkillRepository skillRepository;

  private final SkillMapper skillMapper;
  private final UserRepository userRepository;

  public SkillService(SkillRepository skillRepository, SkillMapper skillMapper, UserRepository userRepository) {
    this.skillRepository = skillRepository;
    this.skillMapper = skillMapper;
    this.userRepository = userRepository;
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

  public void addSkillToUser(String email, String canonicalName) throws ApiException {
    var skill =
      this.skillRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    this.userRepository.addSkill(user.getId(), skill.getCanonicalName());
  }

  public PaginateDto<ReadSkillDto> getUserSkills(String userCanonicalName, Integer page, Integer size) {
    var skills = skillRepository.getUserSkills(userCanonicalName, page * size, size);
    var totalQuantity = skillRepository.getUserSkillsQuantity(userCanonicalName);

    var pagination = new PaginateDto<ReadSkillDto>();
    pagination.setContent(skillMapper.listSkillsToListReadSkillDto(skills));
    pagination.setCurrentPage(page);
    pagination.setTotalPages(PaginationUtils.getPagesQuantity(totalQuantity, size));
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }
}
