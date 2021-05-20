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
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.model.Workplace;
import tech.onlycoders.backend.repository.WorkplaceRepository;

@ExtendWith(MockitoExtension.class)
public class WorkplaceServiceTest {

  @InjectMocks
  private WorkplaceService service;

  @Mock
  private WorkplaceRepository workplaceRepository;

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
}