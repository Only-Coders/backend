package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.onlycoders.backend.dto.user.request.WorkExperienceDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.dto.workposition.request.UpdateWorkPositionDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.mapper.WorkPositionMapperImpl;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.mapper.WorkplaceMapperImpl;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.model.Workplace;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.repository.WorkPositionRepository;
import tech.onlycoders.backend.repository.WorkplaceRepository;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class WorkplaceServiceTest {

  @InjectMocks
  private WorkplaceService service;

  @Mock
  private WorkplaceRepository workplaceRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private WorkPositionRepository workPositionRepository;

  @Spy
  private final WorkPositionMapper workPositionMapper = new WorkPositionMapperImpl(new WorkplaceMapperImpl());

  @Spy
  private final WorkplaceMapper workplaceMapper = Mappers.getMapper(WorkplaceMapper.class);

  private final EasyRandom ezRandom = new EasyRandom();

  @Test
  public void ShouldPaginateOrganizations() {
    var organizations = ezRandom.objects(Workplace.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(organizations);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.workplaceRepository.findByNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listWorkplaces("asd", 1, 1);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewOrganization() {
    var createOrganizationDto = ezRandom.nextObject(CreateWorkplaceDto.class);
    Mockito
      .when(this.workplaceRepository.save(any(Workplace.class)))
      .thenReturn(Workplace.builder().name(createOrganizationDto.getName()).build());
    var result = this.service.createWorkplace(createOrganizationDto);
    assertEquals(createOrganizationDto.getName(), result.getName());
  }

  @Test
  public void ShouldAddWorkingExperience() throws ApiException {
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var organization = ezRandom.nextObject(Workplace.class);
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.workplaceRepository.findById(workExperienceDto.getId())).thenReturn(Optional.of(organization));

    this.service.addWorkExperience(email, workExperienceDto);
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenOrganizationNotFound() {
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.workplaceRepository.findById(workExperienceDto.getId())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWorkExperience(email, workExperienceDto));
  }

  @Test
  public void ShouldFailToAddWorkingExperienceWhenUserNotFound() {
    var organization = ezRandom.nextObject(Workplace.class);
    var workExperienceDto = ezRandom.nextObject(WorkExperienceDto.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.workplaceRepository.findById(workExperienceDto.getId())).thenReturn(Optional.of(organization));
    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.addWorkExperience(email, workExperienceDto));
  }

  @MockitoSettings(strictness = Strictness.LENIENT)
  @Test
  public void ShouldGetWorkingExperiencesUser() {
    var canonicalName = "canonical";
    var page = ezRandom.nextInt();
    var size = ezRandom.nextInt();
    var workPositions = ezRandom.objects(WorkPosition.class, 10).collect(Collectors.toList());
    var readWorkPositionDto = ezRandom.objects(ReadWorkPositionDto.class, 10).collect(Collectors.toList());

    Mockito.when(this.workPositionRepository.getUserJobs(canonicalName, page, size)).thenReturn(workPositions);
    Mockito.when(this.workPositionRepository.countUserJobs(canonicalName)).thenReturn(3);
    Mockito
      .when(this.workPositionMapper.workPositionsToReadWorkPositionDtos(workPositions))
      .thenReturn(readWorkPositionDto);
    var result = this.service.getUserJobs(canonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  public void ShouldRemoveWorkExperience() throws ApiException {
    Mockito.when(this.workPositionRepository.isOwner("email", "id")).thenReturn(true);

    this.service.removeWorkExperience("email", "id");
  }

  @Test
  public void ShouldFailRemoveWorkExperienceWhenIsNotOwner() throws ApiException {
    Mockito.when(this.workPositionRepository.isOwner("email", "id")).thenReturn(false);

    assertThrows(Exception.class, () -> this.service.removeWorkExperience("email", "id"));
  }

  @Test
  public void ShouldUpdateWorkExperience() throws ApiException {
    var workplace = ezRandom.nextObject(Workplace.class);
    var workPosition = ezRandom.nextObject(WorkPosition.class);
    var updateDto = ezRandom.nextObject(WorkExperienceDto.class);
    var workPositionId = ezRandom.nextObject(String.class);
    var email = ezRandom.nextObject(String.class);
    Mockito
      .when(this.workPositionRepository.findUserWorkExperience(email, workPositionId))
      .thenReturn(Optional.of(workPosition));
    Mockito.when(this.workplaceRepository.findById(updateDto.getId())).thenReturn(Optional.of(workplace));

    this.service.updateWorkExperience(email, workPositionId, updateDto);
    assertEquals(workPosition.getPosition(), updateDto.getPosition());
    assertEquals(workPosition.getSince(), updateDto.getSince());
    assertEquals(workPosition.getUntil(), updateDto.getUntil());
  }

  @Test
  public void ShouldFailToUpdateWorkExperience() {
    var updateDto = ezRandom.nextObject(WorkExperienceDto.class);
    var workPositionId = ezRandom.nextObject(String.class);
    var email = ezRandom.nextObject(String.class);
    Mockito
      .when(this.workPositionRepository.findUserWorkExperience(email, workPositionId))
      .thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> this.service.updateWorkExperience(email, workPositionId, updateDto));
  }
}
