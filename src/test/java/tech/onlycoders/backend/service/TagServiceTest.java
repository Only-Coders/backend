package tech.onlycoders.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.mapper.TagMapper;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

  @InjectMocks
  private TagService service;

  @Mock
  private TagRepository tagRepository;

  private final EasyRandom ezRandom = new EasyRandom();

  @Spy
  private final TagMapper tagMapper = Mappers.getMapper(TagMapper.class);

  @Test
  public void ShouldPaginateTags() {
    var tags = ezRandom.objects(Tag.class, 10).collect(Collectors.toList());
    var pages = new PageImpl<>(tags);
    var page = PageRequest.of(1, 1);
    Mockito.when(this.tagRepository.findByCanonicalNameContainingIgnoreCase(anyString(), eq(page))).thenReturn(pages);
    var result = this.service.listTags("asd", 1, 1);
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
}
