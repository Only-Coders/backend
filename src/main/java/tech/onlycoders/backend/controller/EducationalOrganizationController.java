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
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.organization.request.CreateEducationalOrganizationDto;
import tech.onlycoders.backend.dto.organization.request.CreateOrganizationDto;
import tech.onlycoders.backend.dto.organization.response.ReadEducationalOrganizationDto;
import tech.onlycoders.backend.dto.organization.response.ReadOrganizationDto;
import tech.onlycoders.backend.service.EducationalOrganizationService;
import tech.onlycoders.backend.service.OrganizationService;

@RestController
@RequestMapping("/api/educational-organizations")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class EducationalOrganizationController {

  private final EducationalOrganizationService organizationService;

  public EducationalOrganizationController(EducationalOrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PaginatedEducationalOrganizations.class)
          )
        }
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
  @GetMapping
  @Operation(summary = "Search Organizations by name")
  ResponseEntity<PaginateDto<ReadEducationalOrganizationDto>> getOrganizations(
    @RequestParam String organizationName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var pagination = this.organizationService.listEducationalOrganizations(organizationName, page, size);
    return ResponseEntity.ok(pagination);
  }
class PaginatedEducationalOrganizations extends PaginateDto<ReadEducationalOrganizationDto> {}
