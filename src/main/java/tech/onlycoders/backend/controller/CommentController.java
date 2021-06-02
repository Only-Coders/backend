package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.post.request.CreateReactionDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.CommentService;

@RestController
@RequestMapping("/api/comments")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CommentController {

  private final CommentService commentService;

  public CommentController(CommentService commentService) {
    this.commentService = commentService;
  }

  @PostMapping("{commentId}/reactions")
  @Operation(
    summary = "Create a comment reaction",
    description = "If the endpoint is called twice in the same comment then the reaction will be merged"
  )
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> reactToComment(@PathVariable String commentId, @RequestBody CreateReactionDto createReactionDto)
    throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    commentService.reactToComment(userDetails.getCanonicalName(), commentId, createReactionDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("{commentId}/reactions")
  @Operation(
    summary = "Deletes a comment reaction",
    description = "If a reaction exists for the given comment it will get deleted."
  )
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> deleteCommentReaction(@PathVariable String commentId) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    commentService.deleteCommentReaction(userDetails.getCanonicalName(), commentId);
    return ResponseEntity.ok().build();
  }
}
