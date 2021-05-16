package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

  @InjectMocks
  private PostService service;

  @Mock
  private UserRepository personRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final PostMapper postMapper = Mappers.getMapper(PostMapper.class);

  @Test
  public void ShouldCreatePostWhenDataIsOk() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new User();
    var tag = new Tag();
    Mockito.when(personRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(tagRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(tag));
    Mockito.when(postRepository.save(any(Post.class))).thenReturn(new Post());

    var res = service.createPost("canonicalName", requestDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldCreatePostAndTagWhenTagDoesntExist() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new User();
    Mockito.when(personRepository.findByCanonicalName(anyString())).thenReturn(Optional.of(publisher));
    Mockito.when(tagRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());
    Mockito.when(postRepository.save(any(Post.class))).thenReturn(new Post());

    var res = service.createPost("canonicalName", requestDto);
    assertNotNull(res);
  }

  @Test
  public void ShouldFailWhenPublisherDoesntExist() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    var publisher = new User();
    Mockito.when(personRepository.findByCanonicalName(anyString())).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> service.createPost("canonicalName", requestDto));
  }

  @Test
  public void ShouldFailWhenMentionDoesntExist() throws ApiException {
    var requestDto = ezRandom.nextObject(CreatePostDto.class);
    requestDto.setMentionCanonicalNames(new ArrayList<>());
    requestDto.getMentionCanonicalNames().add("wrong");
    var publisher = new User();
    Mockito.when(personRepository.findByCanonicalName("canonicalName")).thenReturn(Optional.of(publisher));
    Mockito.when(personRepository.findByCanonicalName("wrong")).thenReturn(Optional.empty());

    assertThrows(ApiException.class, () -> service.createPost("canonicalName", requestDto));
  }
}
