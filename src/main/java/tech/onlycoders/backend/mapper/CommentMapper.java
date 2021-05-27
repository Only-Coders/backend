package tech.onlycoders.backend.mapper;

import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tech.onlycoders.backend.dto.comment.response.ReadCommentDto;
import tech.onlycoders.backend.dto.contactrequest.response.ReadContactRequestDto;
import tech.onlycoders.backend.model.Comment;
import tech.onlycoders.backend.model.ContactRequest;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = { UserMapper.class })
public interface CommentMapper {
  @Mapping(target = "reactions", ignore = true)
  @Mapping(target = "myReaction", ignore = true)
  ReadCommentDto commentToReadCommentDto(Comment comment);
}
