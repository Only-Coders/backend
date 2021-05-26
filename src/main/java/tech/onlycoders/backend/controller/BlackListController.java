package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.blacklist.response.ReadBlackListDto;
import tech.onlycoders.backend.service.BlackListService;

@RestController
@RequestMapping("/api/blacklist")
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
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginateBlackList.class))
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
}

class PaginateBlackList extends PaginateDto<ReadBlackListDto> {}
