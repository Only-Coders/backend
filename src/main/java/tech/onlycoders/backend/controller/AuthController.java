package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  private final int sessionAge;

  public AuthController(AuthService authService, @Value("${only-coders.jwt.refresh-expires}") Integer sessionAge) {
    this.authService = authService;
    this.sessionAge = sessionAge;
  }

  @PostMapping("/login")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)) }
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
  ResponseEntity<AuthResponseDto> auth(HttpServletResponse response, @RequestBody @Valid AuthRequestDto authRequestDto)
    throws ApiException {
    var authResponse = this.authService.authenticate(authRequestDto);
    var tokenSections = authResponse.getToken().split("\\.");
    setCookie(response, tokenSections);
    return ResponseEntity.ok(AuthResponseDto.builder().token(tokenSections[0] + "." + tokenSections[1]).build());
  }

  private void setCookie(HttpServletResponse response, String[] tokenSections) {
    var sessionCookie = new Cookie("JSESSION", tokenSections[2]);
    sessionCookie.setSecure(true);
    sessionCookie.setHttpOnly(true);
    sessionCookie.setMaxAge(sessionAge);
    response.addCookie(sessionCookie);
  }
}
