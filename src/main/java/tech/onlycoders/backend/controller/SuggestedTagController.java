package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.service.TagService;

@RestController
@RequestMapping("/api/suggested-tags")
@SecurityRequirement(name = "bearerAuth")
public class SuggestedTagController {

  private final TagService tagService;

  public SuggestedTagController(TagService tagService) {
    this.tagService = tagService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ReadUserLiteDto.class))
          )
        }
      )
    }
  )
  @GetMapping
  @Operation(summary = "List suggested tags")
  ResponseEntity getSuggestedTags(@RequestParam(defaultValue = "5") @Min(1) Integer size) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var canonicalName = userDetails.getCanonicalName();
    var tags = this.tagService.getSuggestedTags(canonicalName, size);
    return ResponseEntity.ok(tags);
  }
}
