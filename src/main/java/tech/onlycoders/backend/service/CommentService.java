package tech.onlycoders.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.model.Reaction;
import tech.onlycoders.backend.repository.CommentRepository;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.ReactionRepository;
import tech.onlycoders.backend.repository.UserRepository;

@Service
@Transactional
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ReactionRepository reactionRepository;
  private final PostRepository postRepository;

  public CommentService(
    CommentRepository commentRepository,
    UserRepository userRepository,
    ReactionRepository reactionRepository,
    PostRepository postRepository
  ) {
    this.commentRepository = commentRepository;
    this.userRepository = userRepository;
    this.reactionRepository = reactionRepository;
    this.postRepository = postRepository;
  }

  public void reactToComment(String canonicalName, String commentId, CreateReactionDto createReactionDto)
    throws ApiException {
    var comment =
      this.commentRepository.getById(commentId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.comment-not-found"));
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    this.reactionRepository.getCommentUserReaction(canonicalName, comment.getId())
      .ifPresentOrElse(
        reaction -> {
          this.reactionRepository.updateReaction(reaction.getId(), createReactionDto.getReactionType());
        },
        () -> {
          var reaction = Reaction.builder().type(createReactionDto.getReactionType()).build();
          this.reactionRepository.save(reaction);
          this.reactionRepository.linkWithComment(reaction.getId(), comment.getId());
          this.reactionRepository.linkWithUser(reaction.getId(), user.getId());
        }
      );
  }

  public void deleteCommentReaction(String canonicalName, String commentId) {
    this.reactionRepository.removeCommentReaction(canonicalName, commentId);
  }

  public void deleteComment(String canonicalName, String postId, String commentId) throws ApiException {
    var comment =
      this.commentRepository.getCommentWithPost(commentId, postId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.comment-not-found"));
    if (comment.getPublisher().getCanonicalName().equalsIgnoreCase(canonicalName)) {
      this.commentRepository.deleteById(commentId);
    } else {
      var publisherCanonicalName = postRepository
        .getPostPublisherCanonicalName(postId)
        .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized"));
      if (canonicalName.equalsIgnoreCase(publisherCanonicalName)) {
        this.commentRepository.deleteById(commentId);
      } else {
        throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
      }
    }
  }
}
