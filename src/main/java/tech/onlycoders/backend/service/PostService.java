package tech.onlycoders.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.model.DisplayedTag;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.repository.UserRepository;
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
  private final PostMapper postMapper;
  private final NotificatorService notificatorService;

  public PostService(
    UserRepository userRepository,
    PostRepository postRepository,
    TagRepository tagRepository,
    PostMapper postMapper,
    NotificatorService notificatorService
  ) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.postMapper = postMapper;
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

  public PaginateDto<ReadPostDto> getMyPosts(String email, Integer page, Integer size) {
    return getReadPostDtoPaginateDto(email, page, size);
  }

  private PaginateDto<ReadPostDto> getReadPostDtoPaginateDto(String email, Integer page, Integer size) {
    var skip = page * size;
    var posts = postRepository.getPosts(email, skip, size);

    var totalQuantity = postRepository.countUserPosts(email);
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
      return getReadPostDtoPaginateDto(page, size, posts, totalQuantity);
    }
  }

  public PaginateDto<ReadPostDto> getFeedPosts(String canonicalName, Integer page, Integer size) {
    var totalQuantity = postRepository.getFeedPostsQuantity(canonicalName);
    var skip = page * size;
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var posts = postRepository.getFeedPosts(canonicalName, skip, size);
    //    for (Post post: posts) {
    //      post.setMentions(postRepository.getPostMentions(post.getId()));
    //      post.setTags(postRepository.getPostTags(post.getId()));
    //    }

    var pagination = new PaginateDto<ReadPostDto>();
    pagination.setContent(postMapper.setPostToListPostDto(posts));
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }
}
