package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
import java.util.Optional;
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
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.SkillMapper;
import tech.onlycoders.backend.model.Skill;
import tech.onlycoders.backend.repository.SkillRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class SkillServiceTest {

  @InjectMocks
  private SkillService service;

  @Mock
  private SkillRepository skillRepository;

  @Mock
  private UserRepository userRepository;

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

  @Test
  public void ShouldAddSkill() throws ApiException {
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var skill = ezRandom.nextObject(Skill.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.skillRepository.findById(skill.getCanonicalName())).thenReturn(Optional.of(skill));

    this.service.addSkillToUser(email, skill.getCanonicalName());
  }

  @Test
  public void ShouldFailToAddSchoolWhenSkillNotFound() {
    var skill = ezRandom.nextObject(Skill.class);
    var email = ezRandom.nextObject(String.class);
    assertThrows(ApiException.class, () -> this.service.addSkillToUser(email, skill.getCanonicalName()));
  }

  @Test
  void ShouldReturnUserSkills() {
    var list = new ArrayList<Skill>();
    list.add(ezRandom.nextObject(Skill.class));

    Mockito.when(this.skillRepository.getUserSkillsQuantity(anyString())).thenReturn(1);
    Mockito.when(this.skillRepository.getUserSkills(anyString(), anyInt(), anyInt())).thenReturn(list);

    var res = this.service.getUserSkills("user", 0, 10);

    assertNotNull(res);
  }
}
