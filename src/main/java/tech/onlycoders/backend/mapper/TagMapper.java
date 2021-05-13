package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.model.Tag;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TagMapper {
  ReadTagDto tagToReadTagDto(Tag tag);
  List<ReadTagDto> listTagsToListReadTagDto(List<Tag> tags);
}
