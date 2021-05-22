package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.admin.request.CreateAdminDto;
import tech.onlycoders.backend.dto.admin.response.ReadAdminDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.AdminService;

@RestController
@RequestMapping("/api/admins")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PostMapping
  @PreAuthorize("hasAuthority('ADMIN')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadAdminDto.class)) }
      )
    }
  )
  ResponseEntity<ReadAdminDto> createAdmin(@RequestBody @Valid CreateAdminDto createAdminDto) throws ApiException {
    return ResponseEntity.ok(adminService.createAdmin(createAdminDto));
  }
}
