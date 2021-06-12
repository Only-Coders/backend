package tech.onlycoders.backend.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.model.Post;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { TagMapper.class })
public interface PostMapper {
  @Mapping(target = "commentQuantity", ignore = true)
  @Mapping(target = "reactions", ignore = true)
  @Mapping(target = "myReaction", ignore = true)
  ReadPostDto postToReadPostDto(Post post);

  @Mapping(target = "userFavorites", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "reactions", ignore = true)
  @Mapping(target = "publisher", ignore = true)
  @Mapping(target = "mentions", ignore = true)
  @Mapping(target = "comments", ignore = true)
  Post createPostDtoToPost(CreatePostDto createPostDto);

  List<ReadPostDto> listPostToListPostDto(List<Post> posts);

  List<ReadPostDto> setPostToListPostDto(Set<Post> posts);
}
