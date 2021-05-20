package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.model.User;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {
  ReadUserLiteDto userToReadPersonLiteDto(User person);

  @Mapping(target = "gitProfile", ignore = true)
  ReadUserDto userToReadPersonDto(User person);

  @Mapping(target = "workingPlaces", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "skills", ignore = true)
  @Mapping(target = "securityUpdate", ignore = true)
  @Mapping(target = "schools", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "requests", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "followed", ignore = true)
  @Mapping(target = "email", ignore = true)
  @Mapping(target = "defaultPrivacyIsPublic", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "contacts", ignore = true)
  @Mapping(target = "configs", ignore = true)
  @Mapping(target = "canonicalName", ignore = true)
  @Mapping(target = "blocked", ignore = true)
  User createUserDtoToUser(CreateUserDto createUser);

  List<ReadUserLiteDto> listUserToListReadUserLiteDto(List<User> users);
}
