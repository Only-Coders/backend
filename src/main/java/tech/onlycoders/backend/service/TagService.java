package tech.onlycoders.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.TagMapper;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.CanonicalFactory;
import tech.onlycoders.backend.utils.PaginationUtils;

@Service
@Transactional
public class TagService {

  private final TagRepository tagRepository;
  private final TagMapper tagMapper;
  private final UserRepository userRepository;

  public TagService(TagRepository tagRepository, TagMapper tagMapper, UserRepository userRepository) {
    this.tagRepository = tagRepository;
    this.tagMapper = tagMapper;
    this.userRepository = userRepository;
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

  public PaginateDto<ReadTagDto> listTags(String tagName, Integer page, Integer size, String canonicalName) {
    int totalQuantity;
    List<Tag> tags;
    if (tagName == null) {
      totalQuantity = this.tagRepository.getTagQuantity();
      tags = this.tagRepository.getTagsPaginated(page * size, size);
    } else {
      var regex = "(?i).*" + Pattern.quote(tagName.toLowerCase()) + ".*";
      totalQuantity = this.tagRepository.getTagQuantityByName(regex);
      tags = new ArrayList<>(this.tagRepository.getTagsByNamePaginated(regex, page * size, size));
    }

    return getReadTagDtoPaginateDto(page, size, tags, totalQuantity, canonicalName);
  }

  public void addTagToUser(String email, String canonicalName) throws ApiException {
    var tag =
      this.tagRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.tag-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    this.userRepository.followTag(user.getId(), tag.getCanonicalName());
  }

  public void removeTagFromUser(String email, String canonicalName) throws ApiException {
    var tag =
      this.tagRepository.findById(canonicalName)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.tag-not-found"));
    var user =
      this.userRepository.findByEmail(email)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "error.user-not-found"));
    this.userRepository.unFollowTag(user.getId(), tag.getCanonicalName());
  }

  public PaginateDto<ReadTagDto> getFollowedTags(
    String userCanonicalName,
    String tagCanonicalName,
    Integer page,
    Integer size
  ) {
    var partialCanonicalName = "(?i).*" + tagCanonicalName.toLowerCase() + ".*";
    Set<Tag> tags = this.tagRepository.getFollowedTags(userCanonicalName, partialCanonicalName, page * size, size);
    var totalQuantity = this.tagRepository.getAmountOfFollowedTags(userCanonicalName, partialCanonicalName);
    return getReadTagDtoPaginateDto(page, size, new ArrayList<>(tags), totalQuantity, userCanonicalName);
  }

  private PaginateDto<ReadTagDto> getReadTagDtoPaginateDto(
    Integer page,
    Integer size,
    List<Tag> tags,
    Integer totalQuantity,
    String canonicalName
  ) {
    var pageQuantity = PaginationUtils.getPagesQuantity(totalQuantity, size);

    var readTagDtos = tagMapper.listTagsToListReadTagDto(tags);
    for (ReadTagDto tag : readTagDtos) {
      tag.setFollowerQuantity(this.tagRepository.getFollowerQuantity(tag.getCanonicalName()));
      var following = tagRepository.userFollowsTag(tag.getCanonicalName(), canonicalName);
      tag.setIsFollowing(following);
    }

    var pagination = new PaginateDto<ReadTagDto>();
    pagination.setContent(readTagDtos);
    pagination.setCurrentPage(page);
    pagination.setTotalPages(pageQuantity);
    pagination.setTotalElements(totalQuantity);
    return pagination;
  }

  public List<ReadTagDto> getSuggestedTags(String canonicalName, Integer size) {
    var users = tagRepository.findSuggestedTags(canonicalName, size);
    var readTagDtos = tagMapper.listTagsToListReadTagDto(new ArrayList<>(users));
    readTagDtos
      .stream()
      .parallel()
      .forEach(
        tag -> {
          tag.setFollowerQuantity(this.tagRepository.getFollowerQuantity(tag.getCanonicalName()));
        }
      );
    return readTagDtos;
  }
}
