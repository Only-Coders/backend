package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.recentsearch.request.CreateRecentSearchDto;
import tech.onlycoders.backend.dto.recentsearch.response.ReadRecentSearchDto;
import tech.onlycoders.backend.service.RecentSearchService;

@RestController
@RequestMapping("/api/recent-searches")
@SecurityRequirement(name = "bearerAuth")
public class RecentSearchController {

  private final RecentSearchService recentSearchService;

  public RecentSearchController(RecentSearchService recentSearchService) {
    this.recentSearchService = recentSearchService;
  }

  @PostMapping
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> addRecentSearch(@RequestBody @Valid CreateRecentSearchDto createRecentSearchDto) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    recentSearchService.addRecentSearch(userDetails.getCanonicalName(), createRecentSearchDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ReadRecentSearchDto.class))
          )
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> readRecentSearches() {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var readRecentSearchDtos = recentSearchService.readAllRecentSearch(userDetails.getCanonicalName());
    return ResponseEntity.ok(readRecentSearchDtos);
  }

  @DeleteMapping
  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  ResponseEntity<?> deleteRecentSearches() {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    recentSearchService.clearAllRecentSearch(userDetails.getCanonicalName());
    return ResponseEntity.ok().build();
  }
}
