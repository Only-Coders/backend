package tech.onlycoders.backend.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.model.Admin;

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
}
