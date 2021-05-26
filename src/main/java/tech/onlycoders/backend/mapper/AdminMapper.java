package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadGenericUserDto;
import tech.onlycoders.backend.model.Admin;
import tech.onlycoders.backend.model.Person;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AdminMapper {
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "securityUpdate", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "canonicalName", ignore = true)
  Admin createAdminDtoToPerson(CreateAdminDto createAdminDto);

  ReadAdminDto adminToReadAdminDto(Admin admin);

  List<ReadGenericUserDto> peopleToReadGenericUsers(List<Person> people);
}
