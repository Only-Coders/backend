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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.workplace.request.CreateWorkplaceDto;
import tech.onlycoders.backend.dto.workplace.response.ReadWorkplaceDto;
import tech.onlycoders.backend.service.WorkplaceService;

@RestController
@RequestMapping("/api/workplaces")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class WorkplaceController {

  private final WorkplaceService workplaceService;

  public WorkplaceController(WorkplaceService workplaceService) {
    this.workplaceService = workplaceService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedWorkplaces.class))
        }
      )
    }
  )
  @GetMapping
  @Operation(summary = "Search Workplaces by name")
  ResponseEntity<PaginateDto<ReadWorkplaceDto>> getOrganizations(
    @RequestParam(defaultValue = "", required = false) String workplaceName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var pagination = this.workplaceService.listWorkplaces(workplaceName, page, size);
    return ResponseEntity.ok(pagination);
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ReadWorkplaceDto.class))
        }
      )
    }
  )
  @PostMapping
  @Operation(summary = "Create a Workplace")
  ResponseEntity<ReadWorkplaceDto> createOrganization(@RequestBody CreateWorkplaceDto createWorkplaceDto) {
    var workplace = this.workplaceService.createWorkplace(createWorkplaceDto);
    return ResponseEntity.ok(workplace);
  }
}

class PaginatedWorkplaces extends PaginateDto<ReadWorkplaceDto> {}
