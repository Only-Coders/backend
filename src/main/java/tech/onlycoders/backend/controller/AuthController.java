package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.Duration;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.ApiErrorResponse;
import tech.onlycoders.backend.dto.auth.request.AuthRequestDto;
import tech.onlycoders.backend.dto.auth.response.AuthResponseDto;
import tech.onlycoders.backend.dto.user.request.CreateUserDto;
import tech.onlycoders.backend.exception.ApiException;
import tech.onlycoders.backend.service.AuthService;
import tech.onlycoders.backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final UserService userService;

  private final int sessionAge;
  private final String cookieDomain;
  private final String cookieSameSite;
  private final String cookieName;

  public AuthController(
    AuthService authService,
    UserService userService,
    @Value("${only-coders.jwt.refresh-expires}") Integer sessionAge,
    @Value("${only-coders.cookie.domain}") String cookieDomain,
    @Value("${only-coders.cookie.same-site}") String cookieSameSite,
    @Value("${only-coders.cookie.name}") String cookieName
  ) {
    this.authService = authService;
    this.userService = userService;
    this.sessionAge = sessionAge;
    this.cookieDomain = cookieDomain;
    this.cookieName = cookieName;
    this.cookieSameSite = cookieSameSite;
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
  ResponseEntity<AuthResponseDto> login(
    HttpServletResponse response,
    @RequestBody @Valid AuthRequestDto authRequestDto
  ) throws ApiException {
    var authResponse = this.authService.authenticate(authRequestDto);
    var tokenSections = authResponse.getToken().split("\\.");
    setCookie(response, tokenSections[2], this.sessionAge);
    return ResponseEntity.ok(AuthResponseDto.builder().token(tokenSections[0] + "." + tokenSections[1]).build());
  }

  @PostMapping("/refresh")
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
  ResponseEntity<AuthResponseDto> refresh(
    HttpServletResponse response,
    @RequestHeader("Authorization") String authHeader,
    @CookieValue(value = "JSESSION", required = false) String signatureCookie
  ) throws ApiException {
    var token = authHeader.substring(7);
    if (signatureCookie != null) {
      token = token + "." + signatureCookie;
    }
    var authResponse = this.authService.refreshToken(token);
    var tokenSections = authResponse.getToken().split("\\.");
    setCookie(response, tokenSections[2], this.sessionAge);
    return ResponseEntity.ok(AuthResponseDto.builder().token(tokenSections[0] + "." + tokenSections[1]).build());
  }

  @PostMapping("/logout")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDto.class)) }
      ),
      @ApiResponse(
        responseCode = "401",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  ResponseEntity<?> logout(
    HttpServletResponse response,
    @CookieValue(value = "JSESSION", required = false) String signatureCookie
  ) {
    setCookie(response, "", 0);
    return ResponseEntity.ok().build();
  }

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
      ),
      @ApiResponse(
        responseCode = "409",
        content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
        }
      )
    }
  )
  @PreAuthorize("!hasAnyAuthority('USER','ADMIN')")
  @PostMapping("register")
  @Operation(summary = "Creates a user. Returns a new token and cookie.")
  ResponseEntity<AuthResponseDto> registerUser(
    HttpServletResponse response,
    @RequestBody @Valid CreateUserDto createUserDto
  ) throws ApiException {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var authResponse = this.userService.createUser(email, createUserDto);

    var tokenSections = authResponse.getToken().split("\\.");
    setCookie(response, tokenSections[2], this.sessionAge);
    return ResponseEntity.ok(AuthResponseDto.builder().token(tokenSections[0] + "." + tokenSections[1]).build());
  }

  private void setCookie(HttpServletResponse response, String tokenSignature, int sessionAge) {
    ResponseCookie sessionCookie = ResponseCookie
      .from(cookieName, tokenSignature)
      .httpOnly(true)
      .secure(true)
      .sameSite(cookieSameSite)
      .domain(cookieDomain)
      .path("/")
      .maxAge(Duration.ofSeconds(sessionAge))
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());
  }
}
