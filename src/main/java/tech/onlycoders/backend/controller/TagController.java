package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.tag.request.CreateTagDto;
import tech.onlycoders.backend.dto.tag.response.ReadTagDto;
import tech.onlycoders.backend.service.PostService;
import tech.onlycoders.backend.service.TagService;

@RestController
@RequestMapping("/api/tags")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TagController {

  private final TagService tagService;
  private final PostService postService;

  public TagController(TagService tagService, PostService postService) {
    this.tagService = tagService;
    this.postService = postService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedTags.class)) }
      )
    }
  )
  @GetMapping
  @Operation(summary = "Search tags by name")
  ResponseEntity<PaginateDto<ReadTagDto>> getTags(
    @RequestParam(required = false) String tagName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var pagination = this.tagService.listTags(tagName, page, size);
    return ResponseEntity.ok(pagination);
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadTagDto.class)) }
      )
    }
  )
  @PostMapping
  @Operation(summary = "Create a Tag")
  ResponseEntity<ReadTagDto> createTag(@RequestBody CreateTagDto createTag) {
    var tag = this.tagService.createTag(createTag);
    return ResponseEntity.ok(tag);
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedPosts.class)) }
      )
    }
  )
  @GetMapping("{canonicalName}/posts")
  @Operation(summary = "Get posts related with a tag")
  ResponseEntity<PaginateDto<ReadPostDto>> getTagPosts(
    @PathVariable String canonicalName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var requesterCanonicalName = userDetails.getCanonicalName();
    var pagination = this.postService.getPostsbyTag(requesterCanonicalName, canonicalName, page, size);
    return ResponseEntity.ok(pagination);
  }
}

class PaginatedTags extends PaginateDto<ReadTagDto> {}
