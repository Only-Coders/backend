package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.PaginateDto;
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
      ),
      @ApiResponse(
        responseCode = "400",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      ),
      @ApiResponse(
        responseCode = "403",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<ReadPostDto> newPost(@RequestBody @Valid CreatePostDto createPostDto) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var createdPost = postService.createPost(userDetails.getCanonicalName(), createPostDto);
    return ResponseEntity.ok(createdPost);
  }
}

class PaginatedPosts extends PaginateDto<ReadPostDto> {}
