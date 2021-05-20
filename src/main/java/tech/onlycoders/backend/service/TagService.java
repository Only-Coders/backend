package tech.onlycoders.backend.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.mapper.TagMapper;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
public class TagService {

  private final TagRepository tagRepository;
  private final TagMapper tagMapper;

  public TagService(TagRepository tagRepository, TagMapper tagMapper) {
    this.tagRepository = tagRepository;
    this.tagMapper = tagMapper;
  }

  public ReadTagDto createTag(CreateTagDto createTagDto) {
    var canonicalName = CanonicalFactory.getCanonicalName(createTagDto.getCanonicalName());
    var persistedTag = tagRepository
      .findById(canonicalName)
      .orElseGet(
        () -> {
          var tag = Tag.builder().canonicalName(canonicalName).build();
          tagRepository.save(tag);
          return tag;
        }
      );
    return this.tagMapper.tagToReadTagDto(persistedTag);
  }

  public PaginateDto<ReadTagDto> listTags(String tagName, Integer page, Integer size) {
    int totalQuantity;
    List<Tag> tags;
    if (tagName == null) {
      totalQuantity = this.tagRepository.getTagQuantity();
      tags = this.tagRepository.getTagsPaginated(page * size, size);
    } else {
      var regex = "(?i).*" + tagName.toLowerCase() + ".*";
      totalQuantity = this.tagRepository.getTagQuantityByName(regex);
      tags = this.tagRepository.getTagsByNamePaginated(regex, page * size, size);
    }

    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);
    var readTagDtos = tagMapper.listTagsToListReadTagDto(tags);
    for (ReadTagDto tag : readTagDtos) {
      tag.setFollowerQuantity(this.tagRepository.getFollowerQuantity(tag.getCanonicalName()));
    }

    var pagination = new PaginateDto<ReadTagDto>();
    pagination.setContent(readTagDtos);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }
}
