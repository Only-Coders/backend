package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import tech.onlycoders.backend.dto.organization.request.CreateOrganizationDto;
import tech.onlycoders.backend.mapper.OrganizationMapper;
import tech.onlycoders.backend.model.Organization;
import tech.onlycoders.backend.repository.OrganizationRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceTest {

  @InjectMocks
  private OrganizationService service;

  @Mock
  private OrganizationRepository organizationRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Before
  public void setUp() {
    var mapper = Mappers.getMapper(OrganizationMapper.class);
    ReflectionTestUtils.setField(service, "organizationMapper", mapper);
  }

  @Test
  public void ShouldPaginateOrganizations() {
    var organizations = ezRandom.objects(Organization.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(organizations);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.organizationRepository.findByNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listOrganizations("asd", 1, 1);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewOrganization() {
    var createOrganizationDto = ezRandom.nextObject(CreateOrganizationDto.class);
    Mockito
      .when(this.organizationRepository.save(any(Organization.class)))
      .thenReturn(Organization.builder().name(createOrganizationDto.getName()).build());
    var result = this.service.createOrganization(createOrganizationDto);
    assertEquals(createOrganizationDto.getName(), result.getName());
  }
}
