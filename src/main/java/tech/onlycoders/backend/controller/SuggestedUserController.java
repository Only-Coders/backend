package tech.onlycoders.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import javax.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.onlycoders.backend.bean.auth.UserDetails;
import tech.onlycoders.backend.dto.user.response.ReadUserLiteDto;
import tech.onlycoders.backend.service.UserService;

@RestController
@RequestMapping("/api/suggested-users")
@SecurityRequirement(name = "bearerAuth")
public class SuggestedUserController {

  private final UserService userService;

  public SuggestedUserController(UserService userService) {
    this.userService = userService;
  }

  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        content = {
          @Content(
            mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ReadUserLiteDto.class))
          )
        }
      )
    }
  )
  @GetMapping
  @Operation(summary = "List suggested users")
  ResponseEntity<List<ReadUserLiteDto>> getSuggestedUsers(@RequestParam(defaultValue = "5") @Min(1) Integer size) {
    var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var email = userDetails.getEmail();
    var users = this.userService.getSuggestedUsers(email, size);
    return ResponseEntity.ok(users);
  }
}
