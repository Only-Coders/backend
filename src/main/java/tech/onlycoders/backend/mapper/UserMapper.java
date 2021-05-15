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

  @Mapping(target = "gitProfile", ignore = true)
  User createUserDtoToUser(CreateUserDto createUser);

  List<ReadUserLiteDto> listUserToListReadUserLiteDto(List<User> users);
}
