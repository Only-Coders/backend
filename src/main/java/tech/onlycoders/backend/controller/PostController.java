package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import tech.onlycoders.backend.dto.comment.response.ReadCommentDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
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
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadCommentDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<ReadCommentDto> newComment(
    @PathVariable String id,
    @RequestBody @Valid CreateCommentDto createCommentDto
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ResponseEntity.ok(postService.addComment(userDetails.getCanonicalName(), id, createCommentDto));
  }

  @DeleteMapping("{postId}")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> removePost(@PathVariable String postId) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    postService.removePost(userDetails.getCanonicalName(), postId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("{id}/comments")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedComments.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<PaginateDto<ReadCommentDto>> getPostComments(
    @PathVariable String id,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ResponseEntity.ok(postService.getPostComments(userDetails.getCanonicalName(), id, page, size));
  }

  @PostMapping("{postId}/reactions")
  @Operation(
    summary = "Create a post reaction",
    description = "If the endpoint is called twice in the same post then the reaction will be merged"
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedComments.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> reactToPost(@PathVariable String postId, @RequestBody CreateReactionDto createReactionDto)
    throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    postService.reactToPost(userDetails.getCanonicalName(), postId, createReactionDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("{postId}/reactions")
  @Operation(
    summary = "Deletes a post reaction",
    description = "If a reaction exists for the given post it will get deleted."
  )
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedComments.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> deletePostReaction(@PathVariable String postId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    postService.deletePostReaction(userDetails.getCanonicalName(), postId);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/{postId}")
  @Operation(summary = "Response received update post")
  ResponseEntity<ReadPostDto> updatePost(@PathVariable String postId, @Valid @RequestBody CreatePostDto createPostDto)
    throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var updatedPost = postService.updatePost(postId, userDetails.getCanonicalName(), createPostDto);
    return ResponseEntity.ok(updatedPost);
  }
}

class PaginatedPosts extends PaginateDto<ReadPostDto> {}

class PaginatedComments extends PaginateDto<ReadCommentDto> {}
