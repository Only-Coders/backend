package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.notificationConfiguration.request.UpdateNotificationConfigDto;
import tech.onlycoders.backend.dto.notificationConfiguration.response.ReadNotificationConfigDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.NotificationService;

@RestController
@RequestMapping("/api/notifications-config")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @ApiResponses(value = { @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json") }) })
  @PreAuthorize("hasAuthority('USER')")
  @PutMapping("/{id}")
  @Operation(summary = "Response Boolean")
  ResponseEntity<?> notificationConfiguration(
    @PathVariable String id,
    @RequestBody UpdateNotificationConfigDto updateNotificationConfigDto
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    notificationService.updateStatus(userDetails.getCanonicalName(), updateNotificationConfigDto, id);
    return ResponseEntity.ok().build();
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ReadNotificationConfigDto.class))
          )
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping
  @Operation(summary = "Response Boolean")
  ResponseEntity<List<ReadNotificationConfigDto>> listUserNotificationConfig() {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var response = notificationService.getUserNotificationConfig(userDetails.getCanonicalName());
    return ResponseEntity.ok(response);
  }
}
