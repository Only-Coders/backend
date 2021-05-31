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
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.dto.report.request.CreatePostReportDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.*;
import tech.onlycoders.backend.model.*;
import tech.onlycoders.backend.repository.*;
import tech.onlycoders.backend.utils.PartialPostImpl;
import tech.onlycoders.backend.utils.PartialUserImpl;

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

  @Mock
  private ReportRepository reportRepository;

  @Mock
  private ReportTypeRepository reportTypeRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final PostMapper postMapper = new PostMapperImpl(new TagMapperImpl());

  @Spy
  private final CommentMapper commentMapper = new CommentMapperImpl();

  @Test
  public void ShouldCreatePostWhenDataIsOk() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new PartialUserImpl();
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
    var publisher = new PartialUserImpl();
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
    var publisher = new PartialUserImpl();
    Mockito.when(userRepository.findByCanonicalName("canonicalName")).thenReturn(Optional.of(publisher));
    Mockito.when(userRepository.findByCanonicalName("wrong")).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> service.createPost("canonicalName", requestDto));
  }

  @Test
  public void ShouldViewMyPosts() {
    var canonicalName = ezRandom.nextObject(String.class);
    var postList = new HashSet<Post>();
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
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toSet()));
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
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toSet()));
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
      .thenReturn(ezRandom.objects(Post.class, 10).collect(Collectors.toSet()));
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
      .thenReturn(Optional.of(Reaction.builder().type(ReactionType.APPROVE).build()));
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
    Mockito.when(reactionRepository.getPostUserReaction(anyString(), anyString())).thenReturn(Optional.empty());
    Mockito.when(reactionRepository.getPostReactionQuantity(anyString(), any(ReactionType.class))).thenReturn(1L);

    var result = this.service.getFeedPosts("cname", 0, 10);
    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldAddComment() throws ApiException {
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var commentDto = new CreateCommentDto();
    commentDto.setMessage("message");

    Mockito.when(postRepository.getPostPublisherCanonicalName(anyString())).thenReturn("cname");
    Mockito.when(postRepository.postIsPublic(anyString())).thenReturn(true);
    Mockito.when(userRepository.userIsContact(anyString(), anyString())).thenReturn(true);
    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(user));
    Mockito.when(this.postRepository.getById(anyString())).thenReturn(Optional.of(post));
    Mockito.when(this.reactionRepository.getCommentUserReaction(anyString(), anyString())).thenReturn(null);
    Mockito
      .when(this.reactionRepository.getCommentReactionQuantity(anyString(), any(ReactionType.class)))
      .thenReturn(0L);

    var comment = this.service.addComment("canonical", "postId", commentDto);
    assertNotNull(comment);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldDeletePost() {
    var canonicalName = ezRandom.nextObject(String.class);
    var postId = ezRandom.nextObject(String.class);

    Mockito.doNothing().when(reactionRepository).removePostReaction(anyString(), anyString());
    Mockito.doNothing().when(postRepository).removeCommentsPost(anyString(), anyString());
    Mockito.doNothing().when(postRepository).removeReports(anyString(), anyString());
    Mockito.doNothing().when(postRepository).removePost(anyString(), anyString());

    this.service.removePost(canonicalName, postId);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldReturnPostComments() throws ApiException {
    var list = ezRandom.objects(Comment.class, 10).collect(Collectors.toList());

    Mockito.when(commentRepository.getPostCommentsQuantity(anyString())).thenReturn(0);
    Mockito.when(commentRepository.getPostComments(anyString(), anyInt(), anyInt())).thenReturn(list);
    Mockito.when(this.reactionRepository.getCommentUserReaction(anyString(), anyString())).thenReturn(Optional.empty());
    Mockito
      .when(this.reactionRepository.getCommentReactionQuantity(anyString(), any(ReactionType.class)))
      .thenReturn(0L);
    Mockito.when(postRepository.getPostPublisherCanonicalName(anyString())).thenReturn("cname");
    Mockito.when(postRepository.postIsPublic(anyString())).thenReturn(true);
    Mockito.when(userRepository.userIsContact(anyString(), anyString())).thenReturn(true);

    var result = service.getPostComments("asd", "postid", 0, 10);

    assertNotNull(result);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldFailReturnPostCommentsWhenUserIsNotAllowed() {
    Mockito.when(postRepository.getPostPublisherCanonicalName(anyString())).thenReturn("cname");
    Mockito.when(postRepository.postIsPublic(anyString())).thenReturn(false);
    Mockito.when(userRepository.userIsContact(anyString(), anyString())).thenReturn(false);

    assertThrows(Exception.class, () -> service.getPostComments("asd", "postid", 0, 10));
  }

  @Test
  public void ShouldReportPost() throws ApiException {
    var publisher = new PartialUserImpl();
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var type = new ReportType();

    var dto = CreatePostReportDto.builder().reason("asdas").typeID("asfad").build();

    Mockito.when(this.userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(this.postRepository.getById(anyString())).thenReturn(Optional.of(post));
    Mockito.when(this.reportTypeRepository.findById(anyString())).thenReturn(Optional.of(type));

    this.service.reportPost("cname", "postid", dto);
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldFailDeleteComment() {
    Mockito.when(postRepository.removeComment(anyString(), anyString())).thenReturn(Boolean.FALSE);
    assertThrows(Exception.class, () -> service.removeComment("a", "1"));
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void ShouldDeleteComment() throws ApiException {
    var canonicalName = ezRandom.nextObject(String.class);
    var commentId = ezRandom.nextObject(String.class);

    Mockito.when(postRepository.removeComment(anyString(), anyString())).thenReturn(Boolean.TRUE);

    this.service.removeComment(canonicalName, commentId);
  }

  @Test
  public void ShouldReactToPost() throws ApiException {
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(postRepository.getById(post.getId())).thenReturn(Optional.of(post));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));

    Mockito
      .when(reactionRepository.getPostUserReaction(user.getCanonicalName(), post.getId()))
      .thenReturn(Optional.empty());

    service.reactToPost(user.getCanonicalName(), post.getId(), createReactionDto);
  }

  @Test
  public void ShouldUpdatePostReaction() throws ApiException {
    var reaction = ezRandom.nextObject(Reaction.class);
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var user = ezRandom.nextObject(PartialUserImpl.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(postRepository.getById(post.getId())).thenReturn(Optional.of(post));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.of(user));

    Mockito
      .when(reactionRepository.getPostUserReaction(user.getCanonicalName(), post.getId()))
      .thenReturn(Optional.of(reaction));

    service.reactToPost(user.getCanonicalName(), post.getId(), createReactionDto);
  }

  @Test
  public void ShouldFailToReactToPostPostNotFound() {
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var user = ezRandom.nextObject(User.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(postRepository.getById(post.getId())).thenReturn(Optional.empty());

    assertThrows(Exception.class, () -> service.reactToPost(user.getCanonicalName(), post.getId(), createReactionDto));
  }

  @Test
  public void ShouldFailToReactToPostUserNotFound() {
    var post = ezRandom.nextObject(PartialPostImpl.class);
    var user = ezRandom.nextObject(User.class);
    var createReactionDto = ezRandom.nextObject(CreateReactionDto.class);

    Mockito.when(postRepository.getById(post.getId())).thenReturn(Optional.of(post));
    Mockito.when(userRepository.findByCanonicalName(user.getCanonicalName())).thenReturn(Optional.empty());

    assertThrows(Exception.class, () -> service.reactToPost(user.getCanonicalName(), post.getId(), createReactionDto));
  }

  @Test
  public void ShouldDeletePostReaction() {
    var post = ezRandom.nextObject(Post.class);
    var user = ezRandom.nextObject(User.class);

    Mockito.doNothing().when(reactionRepository).removePostReaction(user.getCanonicalName(), post.getId());

    service.deletePostReaction(user.getCanonicalName(), post.getId());
  }

  @Test
  public void ShouldUpdatePostWhenDataIsOk() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new PartialUserImpl();

    Mockito.when(userRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(postRepository.findById(anyString())).thenReturn(Optional.of(Post.builder().build()));
    Mockito.when(postRepository.save(any(Post.class))).thenReturn(new Post());

    var res = service.updatePost("postId", "canonicalName", requestDto);
    assertNotNull(res);
  }
}
