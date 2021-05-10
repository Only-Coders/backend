package tech.onlycoders.backend.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.person.response.ReadPersonDto;
import tech.onlycoders.backend.dto.person.response.ReadPersonLiteDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.Post;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PersonMapper {
  ReadPersonLiteDto personToReadPersonLiteDto(Person person);
  ReadPersonDto personToReadPersonDto(Person person);
}
