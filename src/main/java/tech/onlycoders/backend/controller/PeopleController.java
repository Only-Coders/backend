package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.person.response.ReadPersonDto;
import tech.onlycoders.backend.dto.post.request.CreatePostDto;
import tech.onlycoders.backend.dto.post.response.ReadPostDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.PersonService;

@RestController
@RequestMapping("/api/people")
@SecurityRequirement(name = "bearerAuth")
public class PeopleController {

  private final PersonService personService;

  public PeopleController(PersonService personService) {
    this.personService = personService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ReadPersonDto.class)) }
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
      ),
      @ApiResponse(
        responseCode = "404",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("hasAuthority('USER')")
  @GetMapping("/{canonicalName}")
  @Operation(summary = "Este endpoint obtiene una personas")
  ResponseEntity<ReadPersonDto> getProfile(@PathVariable String canonicalName) throws ApiException {
    var persistedPerson = this.personService.getProfile(canonicalName);
    return ResponseEntity.ok(persistedPerson);
  }
}
