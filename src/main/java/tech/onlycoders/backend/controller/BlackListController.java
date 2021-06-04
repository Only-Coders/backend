package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.blacklist.response.ReadBlackListDto;
import tech.onlycoders.backend.dto.pagination.PaginatedBlackList;
import tech.onlycoders.backend.service.BlackListService;

@RestController
@RequestMapping("/api/blacklist")
@SecurityRequirement(name = "bearerAuth")
public class BlackListController {

  private final BlackListService blackListService;

  public BlackListController(BlackListService blackListService) {
    this.blackListService = blackListService;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ADMIN')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedBlackList.class))
        }
      )
    }
  )
  ResponseEntity<PaginateDto<ReadBlackListDto>> paginateBlacklist(
    @RequestParam(defaultValue = "", required = false) String partialEmail,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    return ResponseEntity.ok(this.blackListService.paginateBlackList(partialEmail, page, size));
  }

  @DeleteMapping("{email}")
  @PreAuthorize("hasAuthority('ADMIN')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedBlackList.class))
        }
      )
    }
  )
  ResponseEntity<?> paginateBlacklist(@PathVariable String email) {
    this.blackListService.removeUser(email);
    return ResponseEntity.ok().build();
  }
}
