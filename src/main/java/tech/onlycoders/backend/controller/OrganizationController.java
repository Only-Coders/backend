package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.organization.response.ReadOrganizationDto;
import tech.onlycoders.backend.service.OrganizationService;

@RestController
@RequestMapping("/api/organizations")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class OrganizationController {

  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @GetMapping
  @Operation(summary = "Search Organizations by name")
  ResponseEntity<PaginateDto<ReadOrganizationDto>> getOrganizations(
    @RequestParam String organizationName,
    @RequestParam(defaultValue = "0") @Min(0) Integer page,
    @RequestParam(defaultValue = "20") @Min(1) Integer size
  ) {
    var persistedPerson = this.organizationService.listOrganizations(organizationName, page, size);
    return ResponseEntity.ok(persistedPerson);
  }
}
