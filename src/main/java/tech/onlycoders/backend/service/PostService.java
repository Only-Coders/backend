package tech.onlycoders.backend.service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagNameDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.TagRepository;
import tech.onlycoders.backend.repository.UserRepository;
import tech.onlycoders.backend.utils.ProcessingTagLists;

@Service
public class PostService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final PostMapper postMapper;

  public PostService(
    UserRepository userRepository,
    PostRepository postRepository,
    TagRepository tagRepository,
    PostMapper postMapper
  ) {
    this.userRepository = userRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.postMapper = postMapper;
  }

  public ReadPostDto createPost(String publisherCanonicalName, CreatePostDto createPostDto) throws ApiException {
    var publisher = userRepository
      .findByCanonicalName(publisherCanonicalName)
      .orElseThrow(
        () -> new ApiException(HttpStatus.NOT_FOUND, "Couldn't find Person [" + publisherCanonicalName + "]")
      );

    var mentions = getPersonList(createPostDto.getMentionCanonicalNames());
    var tags = getOrSaveTagList(createPostDto.getTagNames());

    var post = postMapper.createPostDtoToPost(createPostDto);
    post.setPublisher(publisher);
    post.setMentions(mentions);
    post.setTags(tags.getPersitedTags());
    post = postRepository.save(post);

    publisher.setDefaultPrivacyIsPublic(post.getIsPublic());
    userRepository.save(publisher);

    var dto = postMapper.postToReadPersonDto(post);
    dto.setTagNames(tags.getTagNames());
    return dto;
  }

  private ProcessingTagLists getOrSaveTagList(List<String> displayTagNames) {
    var titlist = new ProcessingTagLists();
    if (displayTagNames != null) {
      for (String displayName : displayTagNames) {
        var canonicalName = getTagCanonicalName(displayName);
        var tag = tagRepository
          .findByCanonicalName(canonicalName)
          .orElseGet(
            () -> {
              var newTag = Tag.builder().canonicalName(canonicalName).build();
              newTag = tagRepository.save(newTag);
              return newTag;
            }
          );
        titlist.getPersitedTags().add(tag);
        titlist
          .getTagNames()
          .add(ReadTagNameDto.builder().displayName(displayName).canonicalName(canonicalName).build());
      }
    }
    return titlist;
  }

  private String getTagCanonicalName(String name) {
    return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
  }

  private Set<Person> getPersonList(List<String> canonicalNames) throws ApiException {
    var list = new HashSet<Person>();
    if (canonicalNames != null) {
      for (String cName : canonicalNames) {
        var person = userRepository
          .findByCanonicalName(cName)
          .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Couldn't find Person [" + cName + "]"));
        list.add(person);
      }
    }
    return list;
  }
}
