package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.model.Post;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { TagMapper.class })
public interface PostMapper {
  ReadPostDto postToReadPersonDto(Post post);
  Post createPostDtoToPost(CreatePostDto createPostDto);

  List<ReadPostDto> listPostToListPostDto(List<Post> posts);
}
