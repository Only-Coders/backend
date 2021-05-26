package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tech.onlycoders.backend.dto.comment.request.CreateCommentDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.mapper.PostMapperImpl;
import tech.onlycoders.backend.mapper.TagMapperImpl;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

  @InjectMocks
  private PostService service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private NotificatorService notificatorService;

  @Mock
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private ReactionRepository reactionRepository;

  @Mock
  private CommentRepository commentRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final PostMapper postMapper = new PostMapperImpl(new TagMapperImpl());

  @Test
  public void ShouldCreatePostWhenDataIsOk() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new User();
    var tag = new Tag();
    Mockito.when(userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(tagRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(tag));
    Mockito.when(postRepository.save(any(Post.class))).thenReturn(new Post());

    var res = service.createPost("canonicalName", requestDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldCreatePostAndTagWhenTagDoesntExist() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new User();
    Mockito.when(userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(tagRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());
    Mockito.when(postRepository.save(any(Post.class))).thenReturn(new Post());

    var res = service.createPost("canonicalName", requestDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldFailWhenPublisherDoesntExist() {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    Mockito.when(userRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> service.createPost("canonicalName", requestDto));
  }

  @Test
  public void ShouldFailWhenMentionDoesntExist() {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    requestDto.setMentionCanonicalNames(new ArrayList<>());
    requestDto.getMentionCanonicalNames().add("wrong");
    var publisher = new User();
    Mockito.when(userRepository.findByCanonicalName("canonicalName")).thenReturn(Optional.of(publisher));
    Mockito.when(userRepository.findByCanonicalName("wrong")).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> service.createPost("canonicalName", requestDto));
  }

  @Test
  public void ShouldViewMyPosts() {
    var canonicalName = ezRandom.nextObject(String.class);
    var postList = new ArrayList<Post>();
    postList.add(new Post());
    var size = 20;
    var page = 0;

    Mockito.when(this.postRepository.getPosts(canonicalName, page, size)).thenReturn(postList);

    var result = this.service.getMyPosts(canonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  public void ShouldViewCountUserPosts() {
    var requesterCanonicalName = ezRandom.nextObject(String.class);
    var size = 20;
    var page = 0;
    Mockito
      .when(this.postRepository.getPosts(requesterCanonicalName, page, size))
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toList()));
    Mockito.when(this.postRepository.countUserPosts(requesterCanonicalName)).thenReturn(ezRandom.nextInt(10));

    var result = this.service.getPostsOfUser(requesterCanonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldViewPrivatePosts() {
    var requesterCanonicalName = ezRandom.nextObject(String.class);
    var targetCanonicalName = ezRandom.nextObject(String.class);
    var size = 20;
    var page = 0;
    Mockito.when(this.userRepository.userIsContact(requesterCanonicalName, targetCanonicalName)).thenReturn(true);
    Mockito
      .when(this.postRepository.getPosts(requesterCanonicalName, page, size))
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toList()));
    Mockito.when(this.postRepository.countUserPosts(requesterCanonicalName)).thenReturn(ezRandom.nextInt());

    var result = this.service.getUserPosts(requesterCanonicalName, targetCanonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldViewPublicPosts() {
    var requesterCanonicalName = ezRandom.nextObject(String.class);
    var targetCanonicalName = ezRandom.nextObject(String.class);
    var size = 20;
    var page = 0;
    Mockito.when(this.userRepository.userIsContact(requesterCanonicalName, targetCanonicalName)).thenReturn(false);
    Mockito
      .when(this.postRepository.getUserPublicPosts(requesterCanonicalName, page, size))
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toList()));
    Mockito.when(this.postRepository.countUserPublicPosts(requesterCanonicalName)).thenReturn(ezRandom.nextInt());

    var result = this.service.getUserPosts(requesterCanonicalName, targetCanonicalName, page, size);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldReturnFeedPosts() {
    var list = new HashSet<Post>();
    list.add(ezRandom.nextObject(Post.class));

    Mockito.when(postRepository.getFeedPostsQuantity(anyString())).thenReturn(1);
    Mockito.when(postRepository.getFeedPosts(anyString(), anyInt(), anyInt())).thenReturn(list);
    Mockito.when(postRepository.getPostCommentsQuantity(anyString())).thenReturn(0L);
    Mockito
      .when(reactionRepository.getPostUserReaction(anyString(), anyString()))
      .thenReturn(Reaction.builder().type(ReactionType.APPROVE).build());
    Mockito.when(reactionRepository.getPostReactionQuantity(anyString(), any(ReactionType.class))).thenReturn(1L);

    var result = this.service.getFeedPosts("cname", 0, 10);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldReturnFeedPostsWhenNotMyReaction() {
    var list = new HashSet<Post>();
    list.add(ezRandom.nextObject(Post.class));

    Mockito.when(postRepository.getFeedPostsQuantity(anyString())).thenReturn(1);
    Mockito.when(postRepository.getFeedPosts(anyString(), anyInt(), anyInt())).thenReturn(list);
    Mockito.when(postRepository.getPostCommentsQuantity(anyString())).thenReturn(0L);
    Mockito.when(reactionRepository.getPostUserReaction(anyString(), anyString())).thenReturn(null);
    Mockito.when(reactionRepository.getPostReactionQuantity(anyString(), any(ReactionType.class))).thenReturn(1L);

    var result = this.service.getFeedPosts("cname", 0, 10);
    assertNotNull(result);
  }

  @Test
  public void ShouldAddComment() throws ApiException {
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);
    var commentDto = new CreateCommentDto();
    commentDto.setMessage("message");

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.postRepository.findById(anyString())).thenReturn(Optional.of(post));

    this.service.addComment("canonical", "postId", commentDto);
  }
}
