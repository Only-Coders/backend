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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.language.response.ReadLanguageDto;
import tech.onlycoders.backend.service.LanguageService;

@RestController
@RequestMapping("/api/languages")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class LanguageController {

  private final LanguageService service;

  public LanguageController(LanguageService service) {
    this.service = service;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = @Content(
          mediaType = "application/json",
          array = @ArraySchema(schema = @Schema(implementation = ReadLanguageDto.class))
        )
      )
    }
  )
  @GetMapping
  @Operation(summary = "Get languages")
  ResponseEntity<List<ReadLanguageDto>> getLanguages() {
    return ResponseEntity.ok(this.service.getLanguages());
  }
}
