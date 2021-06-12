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
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.dto.datareport.AttributeValueDto;
import tech.onlycoders.backend.dto.datareport.UsersQuantityReportDto;
import tech.onlycoders.backend.service.DataReportService;

@RestController
@RequestMapping("/api/data-reports")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class DataReportController {

  private final DataReportService service;

  public DataReportController(DataReportService dataReportService) {
    this.service = dataReportService;
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = UsersQuantityReportDto.class))
        }
      )
    }
  )
  @GetMapping("users-quantity")
  @Operation(summary = "Return the Active, Blocked and Banned users quantity")
  ResponseEntity<UsersQuantityReportDto> getUsersQuantity() {
    return ResponseEntity.ok(this.service.getUsersQuantity());
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = AttributeValueDto.class))
          )
        }
      )
    }
  )
  @GetMapping("language-use")
  @Operation(summary = "Return a list of languages and user quantity use that uses it")
  ResponseEntity<List<AttributeValueDto>> getLanguageUse() {
    return ResponseEntity.ok(this.service.getLanguageUse());
  }
}
