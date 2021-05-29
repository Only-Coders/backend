package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;

import java.util.Optional;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.TagMapper;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.model.User;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

  @InjectMocks
  private TagService service;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private UserRepository userRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final TagMapper tagMapper = Mappers.getMapper(TagMapper.class);

  @Test
  public void ShouldPaginateTags() {
    var tags = ezRandom.objects(Tag.class, 10).collect(Collectors.toList());

    Mockito.when(this.tagRepository.getTagQuantity()).thenReturn(10);
    Mockito.when(this.tagRepository.getTagsPaginated(anyInt(), anyInt())).thenReturn(tags);
    var result = this.service.listTags(null, 1, 10);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldPaginateTagsByName() {
    var tags = ezRandom.objects(Tag.class, 10).collect(Collectors.toList());

    Mockito.when(this.tagRepository.getTagQuantityByName(anyString())).thenReturn(10);
    Mockito.when(this.tagRepository.getTagsByNamePaginated(anyString(), anyInt(), anyInt())).thenReturn(tags);
    var result = this.service.listTags("asd", 1, 10);
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void ShouldCreateNewTag() {
    var createTagDto = ezRandom.nextObject(CreateTagDto.class);
    Mockito
      .when(this.tagRepository.save(any(Tag.class)))
      .thenReturn(
        Tag.builder().canonicalName(CanonicalFactory.getCanonicalName(createTagDto.getCanonicalName())).build()
      );
    var result = this.service.createTag(createTagDto);
    assertEquals(CanonicalFactory.getCanonicalName(createTagDto.getCanonicalName()), result.getCanonicalName());
  }

  @Test
  public void ShouldAddTag() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    this.service.addTagToUser(email, tag.getCanonicalName());
  }

  @Test
  public void ShouldFailToAddTagWhenTagNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    assertThrows(ApiException.class, () -> this.service.addTagToUser(email, tag.getCanonicalName()));
  }

  @Test
  public void ShouldFailToAddTagWhenUserNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    assertThrows(ApiException.class, () -> this.service.addTagToUser(email, tag.getCanonicalName()));
  }

  @Test
  public void ShouldRemoveTag() throws ApiException {
    var user = ezRandom.nextObject(User.class);
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);

    Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    this.service.removeTagFromUser(email, tag.getCanonicalName());
  }

  @Test
  public void ShouldFailToRemoveTagWhenTagNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    assertThrows(ApiException.class, () -> this.service.removeTagFromUser(email, tag.getCanonicalName()));
  }

  @Test
  public void ShouldFailToRemoveTagWhenUserNotFound() {
    var tag = ezRandom.nextObject(Tag.class);
    var email = ezRandom.nextObject(String.class);
    Mockito.when(this.tagRepository.findById(tag.getCanonicalName())).thenReturn(Optional.of(tag));

    assertThrows(ApiException.class, () -> this.service.removeTagFromUser(email, tag.getCanonicalName()));
  }
}
