package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.mapper.SkillMapper;
import tech.onlycoders.backend.model.Skill;
import tech.onlycoders.backend.repository.SkillRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {

  @InjectMocks
  private SkillService service;

  @Mock
  private SkillRepository skillRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final SkillMapper skillMapper = Mappers.getMapper(SkillMapper.class);

  @Test
  public void ShouldPaginateSkills() {
    var skills = ezRandom.objects(Skill.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(skills);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.skillRepository.findByNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listSkills("asd", 1, 1);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewSkill() {
    var createSkillDto = ezRandom.nextObject(CreateSkillDto.class);
    Mockito
      .when(this.skillRepository.save(any(Skill.class)))
      .thenReturn(
        Skill
          .builder()
          .name(createSkillDto.getName())
          .canonicalName(CanonicalFactory.getCanonicalName(createSkillDto.getName()))
          .build()
      );
    var result = this.service.createSkill(createSkillDto);
    assertEquals(createSkillDto.getName(), result.getName());
  }
}
