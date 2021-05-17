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
import tech.onlycoders.backend.dto.workplace.request.CreateEducationalOrganizationDto;
import tech.onlycoders.backend.mapper.WorkplaceMapper;
import tech.onlycoders.backend.model.EducationalOrganization;
import tech.onlycoders.backend.repository.EducationalOrganizationRepository;

@ExtendWith(MockitoExtension.class)
public class EducationalOrganizationServiceTest {

  @InjectMocks
  private EducationalOrganizationService service;

  @Mock
  private EducationalOrganizationRepository organizationRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final WorkplaceMapper countryMapper = Mappers.getMapper(WorkplaceMapper.class);

  @Test
  public void ShouldPaginateOrganizations() {
    var organizations = ezRandom.objects(EducationalOrganization.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(organizations);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.organizationRepository.findByNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listEducationalOrganizations("asd", 1, 1);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewOrganization() {
    var createOrganizationDto = ezRandom.nextObject(CreateEducationalOrganizationDto.class);
    var org = new EducationalOrganization();
    org.setName(createOrganizationDto.getName());
    Mockito.when(this.organizationRepository.save(any(EducationalOrganization.class))).thenReturn(org);
    var result = this.service.createEducationalOrganization(createOrganizationDto);
    assertEquals(createOrganizationDto.getName(), result.getName());
  }
}
