package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.country.response.ReadCountryDto;
import tech.onlycoders.backend.dto.user.response.ReadUserDto;
import tech.onlycoders.backend.service.CountryService;

@RestController
@RequestMapping("/api/countries")
@SecurityRequirement(name = "bearerAuth")
public class CountryController {

  private final CountryService countryService;

  public CountryController(CountryService countryService) {
    this.countryService = countryService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadUserDto.class)) }
      )
    }
  )
  @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
  @GetMapping
  @Operation(summary = "Search/List countries")
  ResponseEntity<List<ReadCountryDto>> getCountries(@RequestParam(defaultValue = "") String countryName) {
    var countries = this.countryService.findCountries(countryName);
    return ResponseEntity.ok(countries);
  }
}
