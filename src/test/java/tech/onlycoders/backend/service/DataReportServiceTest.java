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
import tech.onlycoders.backend.model.Language;
import tech.onlycoders.backend.model.WorkPosition;
import tech.onlycoders.backend.model.Workplace;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class DataReportServiceTest {

  @InjectMocks
  private DataReportService service;

  @Mock
  private DataReportRepository repoMock;

  @Mock
  private LanguageRepository languageRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Test
  public void ShouldGetUsersQuantity() {
    Mockito.when(repoMock.getUsersQuantity()).thenReturn(1);
    Mockito.when(repoMock.getBlacklistedUsersQuantity()).thenReturn(1);
    Mockito.when(repoMock.getBlockedUsersQuantity()).thenReturn(1);
    var res = service.getUsersQuantity();

    assertNotNull(res);
  }

  @Test
  public void ShouldGetLanguageUse() {
    var list = new ArrayList<Language>();
    list.add(Language.builder().code("en").name("English").build());
    Mockito.when(this.languageRepository.findAll()).thenReturn(list);
    Mockito.when(this.languageRepository.getLanguageUseQuantity(anyString())).thenReturn(1);
    var res = service.getLanguageUse();

    assertNotNull(res);
  }
}
