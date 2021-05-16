package tech.onlycoders.backend.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.tag.response.ReadDisplayedTagDto;
import tech.onlycoders.backend.model.DisplayedTag;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.Tag;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { TagMapper.class })
public interface PostMapper {
  ReadPostDto postToReadPersonDto(Post post);
  Post createPostDtoToPost(CreatePostDto createPostDto);

  List<ReadPostDto> listPostToListPostDto(List<Post> posts);
}
