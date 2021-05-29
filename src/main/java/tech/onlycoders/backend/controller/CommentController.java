package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.PostService;
import tech.onlycoders.backend.service.UserService;

@RestController
@RequestMapping("/api/comment")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

  private final PostService postService;
  private final UserService userService;

  public CommentController(PostService postService, UserService userService) {
    this.postService = postService;
    this.userService = userService;
  }

  @DeleteMapping("{commentId}")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> removeComment(@PathVariable String commentId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    postService.removeComment(userDetails.getCanonicalName(), commentId);
    return ResponseEntity.ok().build();
  }
}
