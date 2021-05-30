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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.PaginateDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.dto.reporttype.response.ReadReportTypeDto;
import tech.onlycoders.backend.service.ReportTypeService;

@RestController
@RequestMapping("/api/report-types")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class ReportTypeController {

  private final ReportTypeService reportTypeService;

  public ReportTypeController(ReportTypeService reportTypeService) {
    this.reportTypeService = reportTypeService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ReadReportTypeDto.class))
          )
        }
      )
    }
  )
  @GetMapping
  @Operation(summary = "Get Report types by language")
  ResponseEntity<List<ReadReportTypeDto>> getOrganizations(@RequestParam(defaultValue = "es") String language) {
    return ResponseEntity.ok(this.reportTypeService.getTypesByLanguage(language));
  }
}
