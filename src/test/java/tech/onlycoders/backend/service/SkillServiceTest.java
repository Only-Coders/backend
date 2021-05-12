package tech.onlycoders.backend.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;

import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import tech.onlycoders.backend.dto.skill.request.CreateSkillDto;
import tech.onlycoders.backend.mapper.SkillMapper;
import tech.onlycoders.backend.model.Skill;
import tech.onlycoders.backend.repository.SkillRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@RunWith(MockitoJUnitRunner.class)
public class SkillServiceTest {

  @InjectMocks
  private SkillService service;

  @Mock
  private SkillRepository skillRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Before
  public void setUp() {
    var mapper = Mappers.getMapper(SkillMapper.class);
    ReflectionTestUtils.setField(service, "skillMapper", mapper);
  }

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
