package tech.onlycoders.backend.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.*;
import tech.onlycoders.backend.dto.tag.response.ReadDisplayedTagDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.model.DisplayedTag;
import tech.onlycoders.backend.model.Tag;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TagMapper {
  @Mapping(source = "tag", target = "canonicalName", qualifiedByName = "tag-to-string")
  @Named("displayed-tag")
  ReadDisplayedTagDto displayedTagToReadDisplayedTagDto(DisplayedTag displayedTag);

  @IterableMapping(qualifiedByName = "displayed-tag")
  List<ReadDisplayedTagDto> setDisplayedTagsToListReadDisplayedTagDto(Set<DisplayedTag> tags);

  @Named("tag-to-string")
  default String tagToString(Tag tag) {
    return tag.getCanonicalName();
  }

  ReadTagDto tagToReadTagDto(Tag tag);

  @IterableMapping(elementTargetType = Tag.class)
  List<ReadTagDto> listTagsToListReadTagDto(List<Tag> tags);
}
