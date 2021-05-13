package tech.onlycoders.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.mapper.TagMapper;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;

@Service
public class TagService {

  private final TagRepository tagRepository;
  private final TagMapper tagMapper;

  public TagService(TagRepository tagRepository, TagMapper tagMapper) {
    this.tagRepository = tagRepository;
    this.tagMapper = tagMapper;
  }

  public ReadTagDto createTag(CreateTagDto createTagDto) {
    var canonicalName = CanonicalFactory.getCanonicalName(createTagDto.getName());
    var tag = Tag.builder().canonicalName(canonicalName).name(createTagDto.getName()).build();
    tagRepository.save(tag);
    return this.tagMapper.tagToReadTagDto(tag);
  }

  public PaginateDto<ReadTagDto> listTags(String tagName, Integer page, Integer size) {
    var pageRequest = PageRequest.of(page, size);
    var paginatedTags = this.tagRepository.findByNameContainingIgnoreCase(tagName, pageRequest);
    var tags = tagMapper.listTagsToListReadTagDto(paginatedTags.getContent());
    var pagination = new PaginateDto<ReadTagDto>();
    pagination.setContent(tags);
    pagination.setCurrentPage(paginatedTags.getNumber());
    pagination.setTotalPages(paginatedTags.getTotalPages());
    pagination.setTotalElements(paginatedTags.getNumberOfElements());
    return pagination;
  }
}
