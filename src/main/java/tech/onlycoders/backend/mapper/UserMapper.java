package tech.onlycoders.backend.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.model.User;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {
  ReadUserLiteDto userToReadPersonLiteDto(User person);

  ReadUserDto userToReadPersonDto(User person);

  User createUserDtoToUser(CreateUserDto createUser);
}
