package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.comment.request.CreateCommentDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.PostService;

@RestController
@RequestMapping("/api/posts")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

  private final PostService postService;

  public PostController(PostService postService) {
    this.postService = postService;
  }

  @PostMapping
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadPostDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<ReadPostDto> newPost(@RequestBody @Valid CreatePostDto createPostDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var createdPost = postService.createPost(userDetails.getCanonicalName(), createPostDto);
    return ResponseEntity.ok(createdPost);
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedPosts.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/user/{canonicalName}")
  ResponseEntity<PaginateDto<ReadPostDto>> getPosts(
    @PathVariable String canonicalName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (canonicalName.equals(userDetails.getCanonicalName())) {
      return ResponseEntity.ok(postService.getMyPosts(canonicalName, page, size));
    } else {
      return ResponseEntity.ok(postService.getUserPosts(userDetails.getCanonicalName(), canonicalName, page, size));
    }
  }

  @PostMapping("{id}/comments")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadPostDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> newComment(@PathVariable String id, @RequestBody @Valid CreateCommentDto createCommentDto)
    throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    postService.addComment(userDetails.getCanonicalName(), id, createCommentDto);
    return ResponseEntity.ok().build();
  }
}

class PaginatedPosts extends PaginateDto<ReadPostDto> {}
