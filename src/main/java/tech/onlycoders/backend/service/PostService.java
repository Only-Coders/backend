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
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.CommentMapper;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.mapper.UserMapper;
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
  private final UserMapper userMapper;
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
    UserMapper userMapper,
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
    this.userMapper = userMapper;
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
    post.setTags(tags);

    final var postId = postRepository.save(post).getId();

    mentions.forEach(partialUser -> postRepository.mentionUser(postId, partialUser.getId()));
    postRepository.linkWithPublisher(post.getId(), publisher.getId());
    userRepository.updateDefaultPrivacy(publisher.getId(), post.getIsPublic());

    var dto = postMapper.postToReadPersonDto(post);
    var publisherDto = this.userMapper.userToReadPersonLiteDto(publisher);
    dto.setPublisher(publisherDto);

    var currentPosition = this.workPositionRepository.getUserCurrentPosition(publisher.getCanonicalName());
    if (currentPosition.isPresent()) {
      var readWorkPositionDto = this.workPositionMapper.workPositionToReadWorkPositionDto(currentPosition.get());
      dto.getPublisher().setCurrentPosition(readWorkPositionDto);
    }

    this.notificatorService.send(
        MessageDTO
          .builder()
          .message(publisher.getFullName() + " ha publicado un nuevo post!")
          .to(publisher.getEmail())
          .eventType(EventType.NEW_POST)
          .build()
      );
    return dto;
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
    var paginatedDto = getReadPostDtoPaginateDto(canonicalName, page, size);
    paginatedDto
      .getContent()
      .parallelStream()
      .forEach(
        post -> {
          post.setMyReaction(getPostUserReaction(canonicalName, post.getId()));
          post.setReactions(getPostReactionQuantity(post.getId()));
          post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
          post.setIsFavorite(postRepository.isFavorite(post.getId(), canonicalName));
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
    Set<Post> posts,
    Integer totalQuantity
  ) {
    var totalPages = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var paginated = new PaginateDto<ReadPostDto>();
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(totalPages);
    paginated.setContent(postMapper.listPostToListPostDto(new ArrayList<>(posts)));

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
    if (userRepository.areUsersConnected(requesterCanonicalName, targetCanonicalName)) {
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

  public PaginateDto<ReadPostDto> getFavoritePosts(String canonicalName, Integer page, Integer size)
    throws ApiException {
    this.userRepository.findByCanonicalName(canonicalName)
      .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error.500"));

    var posts = this.postRepository.getUserFavoritePosts(canonicalName, page * size, size);
    var totalQuantity = this.postRepository.getUserFavoritePostTotalQuantity(canonicalName);
    var pagesQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var medalsCache = new HashMap<String, Integer>();
    var readPostDtoList = postMapper.listPostToListPostDto(posts);
    var paginated = expandPostData(canonicalName, medalsCache, readPostDtoList);
    paginated.setCurrentPage(page);
    paginated.setTotalElements(totalQuantity);
    paginated.setTotalPages(pagesQuantity);
    return paginated;
  }

  private PaginateDto<ReadPostDto> expandPostData(
    String canonicalName,
    HashMap<String, Integer> medalsCache,
    List<ReadPostDto> readPostDtoList
  ) {
    readPostDtoList
      .parallelStream()
      .forEach(
        post -> {
          var publisher = post.getPublisher().getCanonicalName();
          var currentPosition = this.workPositionRepository.getUserCurrentPosition(publisher);
          if (currentPosition.isPresent()) {
            var readWorkPositionDto = this.workPositionMapper.workPositionToReadWorkPositionDto(currentPosition.get());
            post.getPublisher().setCurrentPosition(readWorkPositionDto);
          }
          post.setReactions(getPostReactionQuantity(post.getId()));
          post.setCommentQuantity(postRepository.getPostCommentsQuantity(post.getId()));
          post.setMyReaction(getPostUserReaction(canonicalName, post.getId()));
          post.setIsFavorite(postRepository.isFavorite(post.getId(), canonicalName));
          var medals = getAmountOfMedals(medalsCache, post.getPublisher().getCanonicalName());
          post.getPublisher().setAmountOfMedals(medals);
        }
      );

    var paginated = new PaginateDto<ReadPostDto>();
    paginated.setContent(readPostDtoList);
    return paginated;
  }

  public PaginateDto<ReadPostDto> getFeedPosts(String canonicalName, Integer page, Integer size) {
    var totalQuantity = postRepository.getFeedPostsQuantity(canonicalName);
    var skip = page * size;
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.setPostToListPostDto(postRepository.getFeedPosts(canonicalName, skip, size));

    var medalsCache = new HashMap<String, Integer>();
    var paginated = expandPostData(canonicalName, medalsCache, readPostDtoList);
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
    commentRepository.save(comment);

    postRepository.addComment(post.getId(), comment.getId());
    commentRepository.linkWithCommenter(comment.getId(), commenter.getId());

    var commentDto = commentMapper.commentToReadCommentDto(comment);
    commentDto.setReactions(getCommentReactionQuantity(comment.getId()));
    commentDto.setMyReaction(getCommentUserReaction(canonicalName, comment.getId()));

    //TODO: get post original publisher
    //    this.notificatorService.send(
    //            MessageDTO
    //                    .builder()
    //                    .message(commenter.getFullName() + " ha publicado un comentario en tu post!")
    //                    .to(post.get.getEmail())
    //                    .eventType(EventType.NEW_POST)
    //                    .build()
    //    );

    return commentDto;
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

    var medalsCache = new HashMap<String, Integer>();
    comments
      .parallelStream()
      .forEach(
        readCommentDto -> {
          readCommentDto.setReactions(getCommentReactionQuantity(readCommentDto.getId()));
          readCommentDto.setMyReaction(getCommentUserReaction(requesterCanonicalName, readCommentDto.getId()));
          var medals = getAmountOfMedals(medalsCache, readCommentDto.getPublisher().getCanonicalName());
          readCommentDto.getPublisher().setAmountOfMedals(medals);
        }
      );

    var pagination = new PaginateDto<ReadCommentDto>();
    pagination.setContent(comments);
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
    var publisherCanonicalName = postRepository.getPostPublisherCanonicalName(postId);
    if (
      !publisherCanonicalName.equals(requesterCanonicalName) &&
      !userRepository.areUsersConnected(requesterCanonicalName, requesterCanonicalName) &&
      !postRepository.postIsPublic(postId)
    ) throw new ApiException(HttpStatus.FORBIDDEN, "error.not-authorized");
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
        reaction -> {
          this.reactionRepository.updateReaction(reaction.getId(), createReactionDto.getReactionType());
        },
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
      .findById(postId)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));

    var mentions = getPersonList(createPostDto.getMentionCanonicalNames());

    var message = String.format(
      "%s %s te ha mencionado en un nuevo post.",
      publisher.getFirstName(),
      publisher.getLastName()
    );

    var tags = getOrSaveTagList(createPostDto.getTagNames());

    originalPost.setTags(tags);
    originalPost.setMessage(createPostDto.getMessage());
    originalPost.setIsPublic(createPostDto.getIsPublic());
    originalPost.setUrl(createPostDto.getUrl());
    originalPost.setType(createPostDto.getType());
    originalPost = postRepository.save(originalPost);

    mentions.forEach(partialUser -> postRepository.mentionUser(postId, partialUser.getId()));
    postRepository.linkWithPublisher(originalPost.getId(), publisher.getId());

    mentions.forEach(
      person ->
        this.notificatorService.send(
            MessageDTO.builder().to(person.getEmail()).eventType(EventType.NEW_MENTION).message(message).build()
          )
    );

    return postMapper.postToReadPersonDto(originalPost);
  }

  public void reportPost(String canonicalName, String postId, CreatePostReportDto createPostReportDto)
    throws ApiException {
    var post =
      this.postRepository.getById(postId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.post-not-found"));
    var user =
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
    var totalQuantity = postRepository.getPostsByTagQuantity(requesterCanonicalName, tagCanonicalName);
    var posts = postRepository.getPostsByTag(requesterCanonicalName, tagCanonicalName, page * size, size);

    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readPostDtoList = postMapper.setPostToListPostDto(posts);

    var medalsCache = new HashMap<String, Integer>();

    var pagination = expandPostData(requesterCanonicalName, medalsCache, readPostDtoList);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }
}
