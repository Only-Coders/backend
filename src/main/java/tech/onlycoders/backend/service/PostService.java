package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.ReactionQuantityDto;
import tech.onlycoders.backend.dto.comment.request.CreateCommentDto;
import tech.onlycoders.backend.dto.comment.response.ReadCommentDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.CommentMapper;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PaginationUtils;
import tech.onlycoders.notificator.dto.EventType;
import tech.onlycoders.notificator.dto.MessageDTO;

@Service
@Transactional
public class PostService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final ReactionRepository reactionRepository;
  private final CommentRepository commentRepository;

  private final PostMapper postMapper;
  private final CommentMapper commentMapper;
  private final NotificatorService notificatorService;

  public PostService(
    UserRepository userRepository,
    PostRepository postRepository,
    TagRepository tagRepository,
    ReactionRepository reactionRepository,
    CommentRepository commentRepository,
    PostMapper postMapper,
    CommentMapper commentMapper,
    NotificatorService notificatorService
  ) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.reactionRepository = reactionRepository;
    this.commentRepository = commentRepository;

    this.postMapper = postMapper;
    this.commentMapper = commentMapper;
    this.notificatorService = notificatorService;
  }

  public ReadPostDto createPost(String publisherCanonicalName, CreatePostDto createPostDto) throws ApiException {
    var publisher = userRepository
      .findByCanonicalName(publisherCanonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    var mentions = getPersonList(createPostDto.getMentionCanonicalNames());

    var message = String.format(
      "%s %s te ha mencionado en un nuevo post.",
      publisher.getFirstName(),
      publisher.getLastName()
    );

    mentions.forEach(
      person ->
        this.notificatorService.send(
            MessageDTO.builder().to(person.getEmail()).eventType(EventType.NEW_MENTION).message(message).build()
          )
    );

    var tags = getOrSaveTagList(createPostDto.getTagNames());

    var post = postMapper.createPostDtoToPost(createPostDto);
    post.setPublisher(publisher);
    post.setMentions(mentions);
    post.setTags(tags);
    post = postRepository.save(post);

    publisher.setDefaultPrivacyIsPublic(post.getIsPublic());
    userRepository.save(publisher);
    return postMapper.postToReadPersonDto(post);
  }

  private Set<DisplayedTag> getOrSaveTagList(List<String> displayTagNames) {
    var tagList = new HashSet<DisplayedTag>();
    if (displayTagNames != null) {
      for (String displayName : displayTagNames) {
        var canonicalName = CanonicalFactory.getCanonicalName(displayName);
        var tag = tagRepository
          .findByCanonicalName(canonicalName)
          .orElseGet(
            () -> {
              var newTag = Tag.builder().canonicalName(canonicalName).build();
              newTag = tagRepository.save(newTag);
              return newTag;
            }
          );
        tagList.add(DisplayedTag.builder().displayName(displayName).tag(tag).build());
      }
    }
    return tagList;
  }

  private Set<User> getPersonList(List<String> canonicalNames) throws ApiException {
    var list = new HashSet<User>();
    if (canonicalNames != null) {
      for (String cName : canonicalNames) {
        var person = userRepository
          .findByCanonicalName(cName)
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
        list.add(person);
      }
    }
    return list;
  }

  public PaginateDto<ReadPostDto> getMyPosts(String canonicalName, Integer page, Integer size) {
    var paginatedDto = getReadPostDtoPaginateDto(canonicalName, page, size);
    paginatedDto
      .getContent()
      .parallelStream()
      .forEach(
        post -> {
          post.setMyReaction(getPostUserReaction(canonicalName, post.getId()));
          post.setReactions(getPostReactionQuantity(post.getId()));
          post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
        }
      );
    return paginatedDto;
  }

  private PaginateDto<ReadPostDto> getReadPostDtoPaginateDto(String canonicalName, Integer page, Integer size) {
    var skip = page * size;
    var posts = postRepository.getPosts(canonicalName, skip, size);

    var totalQuantity = postRepository.countUserPosts(canonicalName);
    return getReadPostDtoPaginateDto(page, size, posts, totalQuantity);
  }

  private PaginateDto<ReadPostDto> getReadPostDtoPaginateDto(
    Integer page,
    Integer size,
    List<Post> posts,
    Integer totalQuantity
  ) {
    var totalPages = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var paginated = new PaginateDto<ReadPostDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(totalPages);
    paginated.setContent(postMapper.listPostToListPostDto(posts));

    return paginated;
  }

  public PaginateDto<ReadPostDto> getPostsOfUser(String canonicalName, Integer page, Integer size) {
    return getReadPostDtoPaginateDto(canonicalName, page, size);
  }

  public PaginateDto<ReadPostDto> getUserPosts(
    String requesterCanonicalName,
    String targetCanonicalName,
    Integer page,
    Integer size
  ) {
    if (userRepository.userIsContact(requesterCanonicalName, targetCanonicalName)) {
      return this.getPostsOfUser(targetCanonicalName, page, size);
    } else {
      var skip = page * size;
      var posts = postRepository.getUserPublicPosts(targetCanonicalName, skip, size);

      var totalQuantity = postRepository.countUserPublicPosts(targetCanonicalName);
      var paginatedDto = getReadPostDtoPaginateDto(page, size, posts, totalQuantity);
      paginatedDto
        .getContent()
        .parallelStream()
        .forEach(
          post -> {
            post.setMyReaction(getPostUserReaction(requesterCanonicalName, post.getId()));
            post.setReactions(getPostReactionQuantity(post.getId()));
            post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
          }
        );
      return paginatedDto;
    }
  }

  public PaginateDto<ReadPostDto> getFeedPosts(String canonicalName, Integer page, Integer size) {
    var totalQuantity = postRepository.getFeedPostsQuantity(canonicalName);
    var skip = page * size;
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.setPostToListPostDto(postRepository.getFeedPosts(canonicalName, skip, size));

    readPostDtoList
      .parallelStream()
      .forEach(
        post -> {
          post.setReactions(getPostReactionQuantity(post.getId()));
          post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
          post.setMyReaction(getPostUserReaction(canonicalName, post.getId()));
        }
      );

    var pagination = new PaginateDto<ReadPostDto>();
    pagination.setContent(readPostDtoList);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }

  private ReactionType getPostUserReaction(String canonicalName, String postId) {
    var reaction = reactionRepository.getPostUserReaction(canonicalName, postId);
    return reaction.map(Reaction::getType).orElse(null);
  }

  private List<ReactionQuantityDto> getPostReactionQuantity(String postId) {
    var reactions = new ArrayList<ReactionQuantityDto>();

    reactions.add(
      ReactionQuantityDto
        .builder()
        .reaction(ReactionType.APPROVE)
        .quantity(reactionRepository.getPostReactionQuantity(postId, ReactionType.APPROVE))
        .build()
    );

    reactions.add(
      ReactionQuantityDto
        .builder()
        .reaction(ReactionType.REJECT)
        .quantity(reactionRepository.getPostReactionQuantity(postId, ReactionType.REJECT))
        .build()
    );

    return reactions;
  }

  public ReadCommentDto addComment(String canonicalName, String id, CreateCommentDto createCommentDto)
    throws ApiException {
    validateIsAuthorized(canonicalName, id);

    var commenter = userRepository
      .findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    var post = postRepository
      .findById(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));

    var comment = Comment.builder().publisher(commenter).message(createCommentDto.getMessage()).build();
    commentRepository.save(comment);

    postRepository.addComment(post.getId(), comment.getId());

    var commentDto = commentMapper.commentToReadCommentDto(comment);
    commentDto.setReactions(getCommentReactionQuantity(comment.getId()));
    commentDto.setMyReaction(getCommentUserReaction(canonicalName, comment.getId()));

    return commentDto;
  }

  private List<ReactionQuantityDto> getCommentReactionQuantity(String id) {
    var reactions = new ArrayList<ReactionQuantityDto>();

    reactions.add(
      ReactionQuantityDto
        .builder()
        .reaction(ReactionType.APPROVE)
        .quantity(reactionRepository.getCommentReactionQuantity(id, ReactionType.APPROVE))
        .build()
    );

    reactions.add(
      ReactionQuantityDto
        .builder()
        .reaction(ReactionType.REJECT)
        .quantity(reactionRepository.getCommentReactionQuantity(id, ReactionType.REJECT))
        .build()
    );

    return reactions;
  }

  private ReactionType getCommentUserReaction(String canonicalName, String commentId) {
    var reaction = reactionRepository.getCommentUserReaction(canonicalName, commentId);
    if (reaction != null) return reaction.getType();
    return null;
  }

  public void removePost(String canonicalName, String postId) {
    reactionRepository.removeReaction(canonicalName, postId);
    postRepository.removeCommentsPost(canonicalName, postId);
    postRepository.removeReports(canonicalName, postId);
    postRepository.removePost(canonicalName, postId);
  }

  public PaginateDto<ReadCommentDto> getPostComments(
    String requesterCanonicalName,
    String postId,
    Integer page,
    Integer size
  ) throws ApiException {
    validateIsAuthorized(requesterCanonicalName, postId);

    var totalQuantity = commentRepository.getPostCommentsQuantity(postId);
    var skip = page * size;
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var comments = commentMapper.listCommentToListCommentDto(commentRepository.getPostComments(postId, skip, size));
    for (ReadCommentDto commentDto : comments) {
      commentDto.setReactions(getCommentReactionQuantity(commentDto.getId()));
      commentDto.setMyReaction(getCommentUserReaction(requesterCanonicalName, commentDto.getId()));
    }

    var pagination = new PaginateDto<ReadCommentDto>();
    pagination.setContent(comments);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }

  private void validateIsAuthorized(String requesterCanonicalName, String postId) throws ApiException {
    var publisherCanonicalName = postRepository.getPostPublisherCanonicalName(postId);
    if (
      !publisherCanonicalName.equals(requesterCanonicalName) &&
      !userRepository.userIsContact(requesterCanonicalName, requesterCanonicalName) &&
      !postRepository.postIsPublic(postId)
    ) throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
  }
}
