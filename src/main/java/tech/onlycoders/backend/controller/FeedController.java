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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.service.PostService;

@RestController
@RequestMapping("/api/feed-posts")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class FeedController {

  private final PostService postService;

  public FeedController(PostService postService) {
    this.postService = postService;
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
  @GetMapping
  @Operation(summary = "Search Workplaces by name")
  ResponseEntity<PaginateDto<ReadPostDto>> getOrganizations(
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var userCanonicalName =
      ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getCanonicalName();
    var pagination = this.postService.getFeedPosts(userCanonicalName, page, size);
    return ResponseEntity.ok(pagination);
  }
}
