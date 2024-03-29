package tech.onlycoders.backend.service;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.ReactionQuantityDto;
import tech.onlycoders.backend.dto.comment.request.CreateCommentDto;
import tech.onlycoders.backend.dto.comment.response.ReadCommentDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.report.request.CreatePostReportDto;
import tech.onlycoders.backend.dto.workposition.response.ReadWorkPositionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.CommentMapper;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.mapper.WorkPositionMapper;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.repository.projections.PartialUser;
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
  private final ReportRepository reportRepository;
  private final ReportTypeRepository reportTypeRepository;
  private final WorkPositionRepository workPositionRepository;
  private final WorkPositionMapper workPositionMapper;

  private final PostMapper postMapper;
  private final CommentMapper commentMapper;
  private final NotificatorService notificatorService;

  public PostService(
    UserRepository userRepository,
    PostRepository postRepository,
    TagRepository tagRepository,
    ReactionRepository reactionRepository,
    CommentRepository commentRepository,
    ReportRepository reportRepository,
    ReportTypeRepository reportTypeRepository,
    WorkPositionRepository workPositionRepository,
    WorkPositionMapper workPositionMapper,
    PostMapper postMapper,
    CommentMapper commentMapper,
    NotificatorService notificatorService
  ) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.reactionRepository = reactionRepository;
    this.commentRepository = commentRepository;
    this.reportRepository = reportRepository;
    this.reportTypeRepository = reportTypeRepository;
    this.workPositionRepository = workPositionRepository;
    this.workPositionMapper = workPositionMapper;

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
            MessageDTO
              .builder()
              .to(person.getEmail())
              .eventType(EventType.NEW_MENTION)
              .message(message)
              .from(publisher.getEmail())
              .to(person.getEmail())
              .build()
          )
    );

    var tags = getOrSaveTagList(createPostDto.getTagNames());
    var post = postMapper.createPostDtoToPost(createPostDto);
    post.setTags(tags);

    final var postId = postRepository.save(post).getId();

    mentions.forEach(partialUser -> postRepository.mentionUser(postId, partialUser.getId()));
    postRepository.linkWithPublisher(post.getId(), publisher.getId());
    userRepository.updateDefaultPrivacy(publisher.getId(), post.getIsPublic());

    var dto = postMapper.postToReadPostDto(this.postRepository.getCreatedPost(postId));
    var listDto = Collections.singletonList(dto);

    this.expandPostData(publisherCanonicalName, listDto);

    this.notificatorService.send(
        MessageDTO
          .builder()
          .message(publisher.getFullName() + " ha publicado un nuevo post!")
          .from(publisher.getEmail())
          .to(publisher.getEmail())
          .eventType(EventType.NEW_POST)
          .build()
      );
    return listDto.get(0);
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
              var newTag = Tag.builder().canonicalName(canonicalName).name(displayName).build();
              newTag = tagRepository.save(newTag);
              return newTag;
            }
          );
        tag.setName(displayName);
        tagRepository.save(tag);
        tagList.add(DisplayedTag.builder().displayName(displayName).tag(tag).build());
      }
    }
    return tagList;
  }

  private HashSet<PartialUser> getPersonList(List<String> canonicalNames) throws ApiException {
    var list = new HashSet<PartialUser>();
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
    var posts = postRepository.getPosts(canonicalName, page * size, size);
    var totalQuantity = postRepository.countUserPosts(canonicalName);
    return getReadPostDtoPaginateDto(canonicalName, page, size, posts, totalQuantity);
  }

  public PaginateDto<ReadPostDto> getUserPosts(
    String requesterCanonicalName,
    String targetCanonicalName,
    Integer page,
    Integer size
  ) {
    if (userRepository.areUsersConnected(requesterCanonicalName, targetCanonicalName)) {
      var posts = postRepository.getPosts(targetCanonicalName, page * size, size);
      var totalQuantity = postRepository.countUserPosts(targetCanonicalName);
      return getReadPostDtoPaginateDto(requesterCanonicalName, page, size, posts, totalQuantity);
    } else {
      var skip = page * size;
      var posts = postRepository.getUserPublicPosts(requesterCanonicalName, targetCanonicalName, skip, size);
      var totalQuantity = postRepository.countUserPublicPosts(requesterCanonicalName, targetCanonicalName);
      return getReadPostDtoPaginateDto(requesterCanonicalName, page, size, posts, totalQuantity);
    }
  }

  private PaginateDto<ReadPostDto> getReadPostDtoPaginateDto(
    String sourceCanonicalName,
    Integer page,
    Integer size,
    Set<Post> posts,
    Integer totalQuantity
  ) {
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.listPostToListPostDto(new ArrayList<>(posts));
    var paginated = expandPostData(sourceCanonicalName, readPostDtoList);
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    return paginated;
  }

  public PaginateDto<ReadPostDto> getFavoritePosts(String canonicalName, Integer page, Integer size)
    throws ApiException {
    this.userRepository.findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));

    var posts = this.postRepository.getUserFavoritePosts(canonicalName, page * size, size);
    var totalQuantity = this.postRepository.getUserFavoritePostTotalQuantity(canonicalName);
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.listPostToListPostDto(posts);
    var paginated = expandPostData(canonicalName, readPostDtoList);
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    return paginated;
  }

  private PaginateDto<ReadPostDto> expandPostData(String requesterCanonicalName, List<ReadPostDto> readPostDtoList) {
    HashMap<String, Integer> medalsCache = new HashMap<>();
    HashMap<String, ReadWorkPositionDto> workPositionCache = new HashMap<>();
    readPostDtoList
      .parallelStream()
      .forEach(
        post -> {
          var publisher = post.getPublisher().getCanonicalName();
          var readWorkPositionDto = getUserCurrentPosition(workPositionCache, publisher);
          if (readWorkPositionDto != null) {
            post.getPublisher().setCurrentPosition(readWorkPositionDto);
          }
          post.setReactions(getPostReactionQuantity(post.getId()));
          post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
          post.setMyReaction(getPostUserReaction(requesterCanonicalName, post.getId()));
          post.setIsFavorite(postRepository.isFavorite(post.getId(), requesterCanonicalName));
          var medals = getAmountOfMedals(medalsCache, post.getPublisher().getCanonicalName());
          post.getPublisher().setAmountOfMedals(medals);
        }
      );

    var paginated = new PaginateDto<ReadPostDto>();
    paginated.setContent(readPostDtoList);
    return paginated;
  }

  private ReadWorkPositionDto getUserCurrentPosition(
    HashMap<String, ReadWorkPositionDto> workPositionCache,
    String publisher
  ) {
    if (!workPositionCache.containsKey(publisher)) {
      var currentPosition = this.workPositionRepository.getUserCurrentPosition(publisher);
      if (currentPosition.isPresent()) {
        var readWorkPositionDto = this.workPositionMapper.workPositionToReadWorkPositionDto(currentPosition.get());
        workPositionCache.put(publisher, readWorkPositionDto);
      }
    }
    return workPositionCache.get(publisher);
  }

  private PaginateDto<ReadCommentDto> expandCommentData(
    String requesterCanonicalName,
    List<ReadCommentDto> readCommentDtos
  ) {
    HashMap<String, Integer> medalsCache = new HashMap<>();
    HashMap<String, ReadWorkPositionDto> workPositionCache = new HashMap<>();
    readCommentDtos
      .parallelStream()
      .forEach(
        comment -> {
          var publisher = comment.getPublisher().getCanonicalName();
          var readWorkPositionDto = getUserCurrentPosition(workPositionCache, publisher);
          if (readWorkPositionDto != null) {
            comment.getPublisher().setCurrentPosition(readWorkPositionDto);
          }
          comment.setReactions(getCommentReactionQuantity(comment.getId()));
          comment.setMyReaction(getCommentUserReaction(requesterCanonicalName, comment.getId()));
          var medals = getAmountOfMedals(medalsCache, comment.getPublisher().getCanonicalName());
          comment.getPublisher().setAmountOfMedals(medals);
        }
      );

    var paginated = new PaginateDto<ReadCommentDto>();
    paginated.setContent(readCommentDtos);
    return paginated;
  }

  public PaginateDto<ReadPostDto> getFeedPosts(String canonicalName, Integer page, Integer size) {
    var totalQuantity = postRepository.getFeedPostsQuantity(canonicalName);
    var skip = page * size;
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.setPostToListPostDto(postRepository.getFeedPosts(canonicalName, skip, size));
    var paginated = expandPostData(canonicalName, readPostDtoList);
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pageQuantity);
    return paginated;
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
      .getById(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));

    var comment = Comment.builder().message(createCommentDto.getMessage()).build();
    comment = commentRepository.save(comment);

    postRepository.addComment(post.getId(), comment.getId());
    commentRepository.linkWithCommenter(comment.getId(), commenter.getId());

    var createdComment =
      this.commentRepository.getUserComment(comment.getId(), commenter.getCanonicalName())
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));

    var owner = this.userRepository.getPostOwner(post.getId());

    if (!owner.getEmail().equalsIgnoreCase(commenter.getEmail())) {
      this.notificatorService.send(
          MessageDTO
            .builder()
            .message(commenter.getFullName() + " ha comentado tu post!")
            .to(owner.getEmail())
            .eventType(EventType.NEW_COMMENT)
            .from(commenter.getEmail())
            .build()
        );
    }

    var commentDto = commentMapper.commentToReadCommentDto(createdComment);
    var result = expandCommentData(commenter.getCanonicalName(), List.of(commentDto));
    return result.getContent().get(0);
  }

  public void removeComment(String canonicalName, String commentId) throws ApiException {
    var deleted = this.postRepository.removeComment(canonicalName, commentId);
    if (!deleted) {
      throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
    }
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
    return reaction.map(Reaction::getType).orElse(null);
  }

  public void removePost(String canonicalName, String postId) {
    reactionRepository.removePostReaction(canonicalName, postId);
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

    var pagination = expandCommentData(requesterCanonicalName, comments);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }

  private Integer getAmountOfMedals(HashMap<String, Integer> medalsCache, String userCanonicalName) {
    synchronized (this) {
      if (!medalsCache.containsKey(userCanonicalName)) {
        var medals = userRepository.countUserMedals(userCanonicalName);
        medalsCache.put(userCanonicalName, medals);
      }
      return medalsCache.get(userCanonicalName);
    }
  }

  private void validateIsAuthorized(String requesterCanonicalName, String postId) throws ApiException {
    var requesterIsMentioned = postRepository.userIsMentioned(postId, requesterCanonicalName);
    var postIsPublic = postRepository.postIsPublic(postId);
    if (!postIsPublic) {
      if (!requesterIsMentioned) {
        var publisherCanonicalName = postRepository
          .getPostPublisherCanonicalName(postId)
          .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized"));
        var publisherIsRequester = publisherCanonicalName.equals(requesterCanonicalName);
        if (!publisherIsRequester) {
          var userAreConnected = userRepository.areUsersConnected(publisherCanonicalName, requesterCanonicalName);
          if (!userAreConnected) {
            throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
          }
        }
      }
    }
  }

  public void reactToPost(String canonicalName, String postId, CreateReactionDto createReactionDto)
    throws ApiException {
    var post =
      this.postRepository.getById(postId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));
    var user =
      this.userRepository.findByCanonicalName(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));
    this.reactionRepository.getPostUserReaction(canonicalName, post.getId())
      .ifPresentOrElse(
        reaction -> this.reactionRepository.updateReaction(reaction.getId(), createReactionDto.getReactionType()),
        () -> {
          var reaction = Reaction.builder().type(createReactionDto.getReactionType()).build();
          this.reactionRepository.save(reaction);
          this.reactionRepository.linkWithPost(reaction.getId(), post.getId());
          this.reactionRepository.linkWithUser(reaction.getId(), user.getId());
        }
      );
  }

  public void deletePostReaction(String canonicalName, String postId) {
    this.reactionRepository.removePostReaction(canonicalName, postId);
  }

  public ReadPostDto updatePost(String postId, String canonicalName, CreatePostDto createPostDto) throws ApiException {
    var publisher = userRepository
      .findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));

    var originalPost = postRepository
      .getById(postId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));

    var mentions = getPersonList(createPostDto.getMentionCanonicalNames());

    postRepository.removePostTags(originalPost.getId());
    postRepository.removePostMentions(originalPost.getId());

    var tags = getOrSaveTagList(createPostDto.getTagNames());
    tags.forEach(tag -> postRepository.mentionTag(postId, tag.getTag().getCanonicalName(), tag.getDisplayName()));

    var newMessage = createPostDto.getMessage();
    var newPrivacy = createPostDto.getIsPublic();
    var newUrl = createPostDto.getUrl();
    var newType = createPostDto.getType();

    postRepository.updatePost(originalPost.getId(), newMessage, newPrivacy, newUrl, newType);

    mentions.forEach(partialUser -> postRepository.mentionUser(postId, partialUser.getId()));
    postRepository.linkWithPublisher(originalPost.getId(), publisher.getId());

    var updatedPost = postRepository.getCreatedPost(originalPost.getId());

    return postMapper.postToReadPostDto(updatedPost);
  }

  public void reportPost(String canonicalName, String postId, CreatePostReportDto createPostReportDto)
    throws ApiException {
    this.postRepository.getById(postId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));
    this.userRepository.findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));

    var reportType =
      this.reportTypeRepository.findById(createPostReportDto.getTypeID())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.report-type-not-found"));

    var report = Report.builder().reason(createPostReportDto.getReason()).type(reportType).build();
    reportRepository.save(report);
    reportRepository.linkReportToPost(postId, report.getId());
    reportRepository.linkReportToUser(canonicalName, report.getId());
  }

  public PaginateDto<ReadPostDto> getPostsByTag(
    String requesterCanonicalName,
    String tagCanonicalName,
    Integer page,
    Integer size
  ) {
    var totalQuantity = postRepository.countPostsByTag(requesterCanonicalName, tagCanonicalName);
    var posts = postRepository.getPostsByTag(requesterCanonicalName, tagCanonicalName, page * size, size);

    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.setPostToListPostDto(posts);

    var pagination = expandPostData(requesterCanonicalName, readPostDtoList);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }
}
