package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.comment.response.ReadCommentDto;
import tech.onlycoders.backend.model.Comment;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { UserMapper.class })
public interface CommentMapper {
  @Mapping(target = "reactions", ignore = true)
  @Mapping(target = "myReaction", ignore = true)
  ReadCommentDto commentToReadCommentDto(Comment comment);

  List<ReadCommentDto> listCommentToListCommentDto(List<Comment> postComments);
}
