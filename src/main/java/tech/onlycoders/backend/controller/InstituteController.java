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
import tech.onlycoders.backend.dto.institute.request.CreateInstituteDto;
import tech.onlycoders.backend.dto.institute.response.ReadInstituteDto;
import tech.onlycoders.backend.service.InstituteService;

@RestController
@RequestMapping("/api/institutes")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class InstituteController {

  private final InstituteService instituteService;

  public InstituteController(InstituteService instituteService) {
    this.instituteService = instituteService;
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedInstitutes.class))
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
  @Operation(summary = "Search Institutes by name")
  ResponseEntity<PaginateDto<ReadInstituteDto>> getInstitutes(
    @RequestParam(defaultValue = "", required = false) String instituteName,
    @RequestParam(defaultValue = "0", required = false) @Min(0) Integer page,
    @RequestParam(defaultValue = "20", required = false) @Min(1) Integer size
  ) {
    var pagination = this.instituteService.listInstitutes(instituteName, page, size);
    return ResponseEntity.ok(pagination);
  }

  @PreAuthorize("hasAuthority('USER')")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ReadInstituteDto.class))
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
  @PostMapping
  @Operation(summary = "Create an Institute")
  ResponseEntity<ReadInstituteDto> createInstitute(@RequestBody CreateInstituteDto createInstituteDto) {
    var instituteDto = this.instituteService.createInstitute(createInstituteDto);
    return ResponseEntity.ok(instituteDto);
  }
}

class PaginatedInstitutes extends PaginateDto<ReadInstituteDto> {}
