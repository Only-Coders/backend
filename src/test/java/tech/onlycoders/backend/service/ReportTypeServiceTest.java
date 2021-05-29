package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

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
import tech.onlycoders.backend.bean.FirebaseService;
import tech.onlycoders.backend.dto.RoleEnum;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.AdminMapper;
import tech.onlycoders.backend.mapper.ReportTypeMapper;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.ReportType;
import tech.onlycoders.backend.model.Role;
import tech.onlycoders.backend.repository.AdminRepository;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.ReportTypeRepository;
import tech.onlycoders.backend.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
public class ReportTypeServiceTest {

  @InjectMocks
  private ReportTypeService service;

  @Mock
  private ReportTypeRepository reportTypeRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final ReportTypeMapper reportTypeMapper = Mappers.getMapper(ReportTypeMapper.class);

  @Test
  public void ShouldReturnTypesByLanguage() {
    var type = new ReportType();
    var list = new ArrayList<ReportType>();
    list.add(type);

    Mockito.when(this.reportTypeRepository.findAllByLanguage(anyString())).thenReturn(list);

    var result = this.service.getTypesByLanguage("es");
    assertNotNull(result);
  }
}
