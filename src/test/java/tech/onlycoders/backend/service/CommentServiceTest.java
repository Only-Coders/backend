package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Comment;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.Reaction;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.CommentRepository;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.ReactionRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.PartialCommentImpl;
import tech.onlycoders.backend.utils.PartialUserImpl;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @InjectMocks
  private CommentService service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private ReactionRepository reactionRepository;

  @Mock
  private CommentRepository commentRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Test
  public void ShouldReactToPost() throws ApiException {
    var comment = ezRandom.nextObject(PartialCommentImpl.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));

    Mockito
      .when(reactionRepository.getCommentUserReaction(user.getCanonicalName(), comment.getId()))
      .thenReturn(Optional.empty());

    service.reactToComment(user.getCanonicalName(), comment.getId(), createReactionDto);
  }

  @Test
  public void ShouldUpdatePostReaction() throws ApiException {
    var reaction = ezRandom.nextObject(Reaction.class);
    var comment = ezRandom.nextObject(PartialCommentImpl.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));

    Mockito
      .when(reactionRepository.getCommentUserReaction(user.getCanonicalName(), comment.getId()))
      .thenReturn(Optional.of(reaction));

    service.reactToComment(user.getCanonicalName(), comment.getId(), createReactionDto);
  }

  @Test
  public void ShouldFailToReactToPostPostNotFound() {
    var comment = ezRandom.nextObject(PartialCommentImpl.class);
    var user = ezRandom.nextObject(User.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.empty());

    assertThrows(
      Exception.class,
      () -> service.reactToComment(user.getCanonicalName(), comment.getId(), createReactionDto)
    );
  }

  @Test
  public void ShouldFailToReactToPostUserNotFound() {
    var comment = ezRandom.nextObject(PartialCommentImpl.class);
    var user = ezRandom.nextObject(User.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(commentRepository.getById(comment.getId())).thenReturn(Optional.of(comment));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(
      Exception.class,
      () -> service.reactToComment(user.getCanonicalName(), comment.getId(), createReactionDto)
    );
  }

  @Test
  public void ShouldDeleteCommentReaction() {
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);
    Mockito.doNothing().when(reactionRepository).removeCommentReaction(user.getCanonicalName(), post.getId());

    service.deleteCommentReaction(user.getCanonicalName(), post.getId());
  }

  @Test
  public void ShouldDeleteOwnComment() throws ApiException {
    var comment = ezRandom.nextObject(Comment.class);
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);
    comment.setPublisher(user);

    Mockito.when(commentRepository.getCommentWithPost(comment.getId(), post.getId())).thenReturn(Optional.of(comment));
    service.deleteComment(user.getCanonicalName(), post.getId(), comment.getId());
  }

  @Test
  public void ShouldDeleteAnyOnesCommentIfIsMyPost() throws ApiException {
    var comment = ezRandom.nextObject(Comment.class);
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);

    Mockito.when(commentRepository.getCommentWithPost(comment.getId(), post.getId())).thenReturn(Optional.of(comment));
    Mockito
      .when(postRepository.getPostPublisherCanonicalName(post.getId()))
      .thenReturn(Optional.of(user.getCanonicalName()));
    service.deleteComment(user.getCanonicalName(), post.getId(), comment.getId());
  }

  @Test
  public void ShouldNotDeleteCommentIfIsNotMyPostOrComment() throws ApiException {
    var comment = ezRandom.nextObject(Comment.class);
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);

    Mockito.when(commentRepository.getCommentWithPost(comment.getId(), post.getId())).thenReturn(Optional.of(comment));
    Mockito.when(postRepository.getPostPublisherCanonicalName(post.getId())).thenReturn(Optional.of("random"));
    assertThrows(
      ApiException.class,
      () -> this.service.deleteComment(user.getCanonicalName(), post.getId(), comment.getId())
    );
  }

  @Test
  public void ShouldNotDeleteCommentIfPublisherNotFound() throws ApiException {
    var comment = ezRandom.nextObject(Comment.class);
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);

    Mockito.when(commentRepository.getCommentWithPost(comment.getId(), post.getId())).thenReturn(Optional.of(comment));
    Mockito.when(postRepository.getPostPublisherCanonicalName(post.getId())).thenReturn(Optional.empty());
    assertThrows(
      ApiException.class,
      () -> this.service.deleteComment(user.getCanonicalName(), post.getId(), comment.getId())
    );
  }
}
