package tech.onlycoders.backend.service;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.mapper.PostMapper;
import tech.onlycoders.backend.model.Person;
import tech.onlycoders.backend.model.Post;
import tech.onlycoders.backend.model.Tag;
import tech.onlycoders.backend.repository.PersonRepository;
import tech.onlycoders.backend.repository.PostRepository;
import tech.onlycoders.backend.repository.TagRepository;

@Service
public class PostService {

  private PersonRepository personRepository;
  private PostRepository postRepository;
  private TagRepository tagRepository;
  private PostMapper postMapper;

  public PostService(
    PersonRepository personRepository,
    PostRepository postRepository,
    TagRepository tagRepository,
    PostMapper postMapper
  ) {
    this.personRepository = personRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.postMapper = postMapper;
  }

  public ReadPostDto createPost(String publisherCanonicalName, CreatePostDto createPostDto) throws ApiException {
    var optionalPublisher = personRepository.findByCanonicalName(publisherCanonicalName);
    if (!optionalPublisher.isPresent()) {
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn`t find Person [" + publisherCanonicalName + "]");
    }
    var publisher = optionalPublisher.get();

    var mentions = getPersonList(createPostDto.getMentionCanonicalNames());
    var tags = getOrSaveTagList(createPostDto.getTagNames());

    var post = postMapper.createPostDtoToPost(createPostDto);
    post.setPublisher(publisher);
    post.setMentions(mentions);
    post.setTags(tags);
    post = postRepository.save(post);

    publisher.setDefaultPrivacyIsPublic(post.getIsPublic());
    personRepository.save(publisher);

    return postMapper.postToReadPersonDto(post);
  }

  private Set<Tag> getOrSaveTagList(List<String> tagNames) {
    var list = new HashSet<Tag>();
    if (tagNames != null) {
      for (String name : tagNames) {
        var cName = getTagCanonicalName(name);
        var optTag = tagRepository.findByCanonicalName(cName);
        if (optTag.isPresent()) {
          list.add(optTag.get());
        } else {
          var tag = new Tag(cName, name);
          tag = tagRepository.save(tag);
          list.add(tag);
        }
      }
    }
    return list;
  }

  private String getTagCanonicalName(String name) {
    return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\W", "").toLowerCase();
  }

  private Set<Person> getPersonList(List<String> canonicalNames) throws ApiException {
    var list = new HashSet<Person>();
    if (canonicalNames != null) {
      for (String cName : canonicalNames) {
        var optionalPerson = personRepository.findByCanonicalName(cName);
        if (!optionalPerson.isPresent()) {
          throw new ApiException(HttpStatus.BAD_REQUEST, "Couldn`t find Person [" + cName + "]");
        }
        list.add(optionalPerson.get());
      }
    }
    return list;
  }
}
